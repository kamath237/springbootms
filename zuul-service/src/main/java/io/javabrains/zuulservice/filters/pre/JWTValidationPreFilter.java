package io.javabrains.zuulservice.filters.pre;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.nimbusds.jwt.SignedJWT;
import com.sun.jersey.core.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Enumeration;

@Component
public class JWTValidationPreFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(JWTValidationPreFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private void sendResponse(int responseCode, String responseBody) {
        final RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseBody("{ \"error\": "+ "\""+responseBody+"\"}");
        ctx.getResponse().setContentType("application/json");
        ctx.setResponseStatusCode(responseCode);


       /* try {
            ctx.getResponse().sendError(responseCode, responseBody);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }*/
    }

    public static PublicKey getPublicKey()
            throws CertificateException, IOException {
        String x5c = "MIICsDCCAhkCBQDzl9JoMA0GCSqGSIb3DQEBCwUAMIGbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRQwEgYDVQQKEwtPYmxpeCwgSW5jLjERMA8GA1UECxMITmV0UG9pbnQxOjA4BgNVBAMTMU5ldFBvaW50IFNpbXBsZSBTZWN1cml0eSBDQSAtIE5vdCBmb3IgR2VuZXJhbCBVc2UwHhcNMjAwNzIwMDkwNjIyWhcNMjUwNzE5MDkwNjIyWjAdMRswGQYDVQQDExJPSURDV2ViR2F0ZURvbWFpbjYwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDY4jFeMgfOq5zei/u3u0SX9khMR2l2jvmouMJoAY2vwp0IvDMXGojv/WpHWxlNjiQsc6X78YVJLJ9Pnf0tj060lYMSjwRor19l014QOL7FBbmQ8+lnphv6xTMa2mF8MiwhAuP2+n2tbCAtFIi/MlgZmuYd8jYGEQK3r3qgtMS3NrExPXJFnEBEzA+14O/rgGhQwcOb5eGNBk9Ei3V2DRjDvyCF8oqoPT9XwlLdREmj7LT3/pm0KHXh7f/uE6xeYcBP1xFvfKy6sTmboyviPz4dKXjALWP+cL1VZLitjQkzf4SAxAglvEJZ/2qYndBgLfibsmwGUbffywVSJ9CgUmtxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAM88Uw0WXmBQtAyCAp4EtcS4aty1Ap94USKsgWwa7IidGZA77oDKw+w1VcdCWUDBsGwOmXvkW2KFyPD3lXURb1LeKitU1orzldme5/hSbRHUlE3YZ4x2lxGrRdERFhAurI6Ci7eRyL4Bsuya4sV5SxFOK0wp1IKdlStAlbpwExYs=";
        byte[] keyBytes = Base64.decode(x5c);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(keyBytes));
        return cer.getPublicKey();

    }

    @Override
    public Object run() throws ZuulException {
        System.out.println(">>>>....inseide zuul filter run method");
        // unpack header to determine whether it's a trusted entity (?)

        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        final String getHeader = request.getHeader("Authorization");

        final Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            final String headerName = headers.nextElement();
            final String header = request.getHeader(headerName);

            log.info(headerName+ "=" +header);
        }

        log.trace(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        if (getHeader == null || getHeader.equals("null")) {
            log.debug("Missing authentication header!");

            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication header!");
            return null;
        }

        // the header should be like "Bearer askjdhgkjasdfhkdaslhgkadsjhkd"
        if (!getHeader.toLowerCase().startsWith("bearer")) {
            log.debug("Invalid authentication header!");

            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication header!");
            return null;
        }
        // split the string after the bearer and validate it
        final String[] arr = getHeader.split("\\s+");
        log.info("Header array length is: " + arr.length);

        if (arr.length < 2) {
            log.debug("Invalid authentication header!");
            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication header!");
            return null;
        }

        final String jwt = arr[1];
        log.trace("Encoded token is: " + jwt);
        try {
            final SignedJWT signedJWT = SignedJWT.parse(jwt);

            log.info("Issuer:" + signedJWT.getJWTClaimsSet().getIssuerClaim());
            log.info("Issue time:" + signedJWT.getJWTClaimsSet().getIssuedAtClaim());
            log.info("Expiration time:" + signedJWT.getJWTClaimsSet().getExpirationTimeClaim());
            log.info("Not Before time:" + signedJWT.getJWTClaimsSet().getNotBeforeClaim());
            log.info("Audience:" + signedJWT.getJWTClaimsSet().getAudienceClaim());


            // verify the claims
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) getPublicKey(), null);
            Verification verifier = JWT.require(algorithm);
            DecodedJWT decodedJwt = JWT.decode(jwt);

            verifier.build().verify(decodedJwt);

/*            final JWSVerifier verifier = new MACVerifier(Files.readAllBytes(Paths.get("D:\\oam-domain-cert.cer")));

            if (!signedJWT.verify(verifier)) {
                log.debug("Unable to verify JWT");
                sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unable to verify JWT token");
                return null;
            }*/

            log.info("JWT token is valid!");

            // issuer must be "apic"
            if (signedJWT.getJWTClaimsSet().getIssuerClaim() == null ||
                    !signedJWT.getJWTClaimsSet().getIssuerClaim().equals("http://mftcqa16ohs.temenosgroup.com:40201/oauth2")) {
                log.debug("Invalid issuer!");
                sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unable to verify JWT token");
                return null;
            }

            if (signedJWT.getJWTClaimsSet().getExpirationTimeClaim() == Long.valueOf("0") ||
                    signedJWT.getJWTClaimsSet().getExpirationTimeClaim() < (Calendar.getInstance().getTimeInMillis()/1000)) {
                log.debug("JWT Token expired!");
                sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired.. Expiry:  "+signedJWT.getJWTClaimsSet().getExpirationTimeClaim()+" Current time: "+(Calendar.getInstance().getTimeInMillis())/1000);
                return null;
            }

            if (signedJWT.getJWTClaimsSet().getNotBeforeClaim() != 0L &&
                    signedJWT.getJWTClaimsSet().getNotBeforeClaim()>(Calendar.getInstance().getTimeInMillis())) {
                log.debug("JWT Token not valid!");
                sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "JWT token invalid");
                return null;
            }
        } catch (ParseException e) {
            log.error("Parse exception: " + e.getMessage());
            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        } /*catch (JOSEException e) {
            log.error("JOSEException: " + e.getMessage());
            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        } */catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            log.error("CertificateException: " + e.getMessage());
            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Failed to retrieve keys for token verification");
        }catch (JWTVerificationException exception){
            //Invalid signature/claims
            log.error("JWTVerificationException: " + exception.getMessage());
            sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Failed to verify token: "+exception.getMessage());
        }

        return null;
    }
}
