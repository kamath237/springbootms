package io.javabrains.zuulservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.sun.jersey.core.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

public class TestPublicKeyExtractor {


    public static PublicKey getPublicKey()
            throws CertificateException, IOException {
        String x5c = "MIICsDCCAhkCBQDzl9JoMA0GCSqGSIb3DQEBCwUAMIGbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRQwEgYDVQQKEwtPYmxpeCwgSW5jLjERMA8GA1UECxMITmV0UG9pbnQxOjA4BgNVBAMTMU5ldFBvaW50IFNpbXBsZSBTZWN1cml0eSBDQSAtIE5vdCBmb3IgR2VuZXJhbCBVc2UwHhcNMjAwNzIwMDkwNjIyWhcNMjUwNzE5MDkwNjIyWjAdMRswGQYDVQQDExJPSURDV2ViR2F0ZURvbWFpbjYwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDY4jFeMgfOq5zei/u3u0SX9khMR2l2jvmouMJoAY2vwp0IvDMXGojv/WpHWxlNjiQsc6X78YVJLJ9Pnf0tj060lYMSjwRor19l014QOL7FBbmQ8+lnphv6xTMa2mF8MiwhAuP2+n2tbCAtFIi/MlgZmuYd8jYGEQK3r3qgtMS3NrExPXJFnEBEzA+14O/rgGhQwcOb5eGNBk9Ei3V2DRjDvyCF8oqoPT9XwlLdREmj7LT3/pm0KHXh7f/uE6xeYcBP1xFvfKy6sTmboyviPz4dKXjALWP+cL1VZLitjQkzf4SAxAglvEJZ/2qYndBgLfibsmwGUbffywVSJ9CgUmtxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAM88Uw0WXmBQtAyCAp4EtcS4aty1Ap94USKsgWwa7IidGZA77oDKw+w1VcdCWUDBsGwOmXvkW2KFyPD3lXURb1LeKitU1orzldme5/hSbRHUlE3YZ4x2lxGrRdERFhAurI6Ci7eRyL4Bsuya4sV5SxFOK0wp1IKdlStAlbpwExYs=";
        byte[] keyBytes = Base64.decode(x5c);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(keyBytes));
        return cer.getPublicKey();

    }


        public static void main(String args[]) throws IOException {
            byte[] keyBytes =
                    "MIICsDCCAhkCBQDzl9JoMA0GCSqGSIb3DQEBCwUAMIGbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTESMBAGA1UEBxMJQ3VwZXJ0aW5vMRQwEgYDVQQKEwtPYmxpeCwgSW5jLjERMA8GA1UECxMITmV0UG9pbnQxOjA4BgNVBAMTMU5ldFBvaW50IFNpbXBsZSBTZWN1cml0eSBDQSAtIE5vdCBmb3IgR2VuZXJhbCBVc2UwHhcNMjAwNzIwMDkwNjIyWhcNMjUwNzE5MDkwNjIyWjAdMRswGQYDVQQDExJPSURDV2ViR2F0ZURvbWFpbjYwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDY4jFeMgfOq5zei/u3u0SX9khMR2l2jvmouMJoAY2vwp0IvDMXGojv/WpHWxlNjiQsc6X78YVJLJ9Pnf0tj060lYMSjwRor19l014QOL7FBbmQ8+lnphv6xTMa2mF8MiwhAuP2+n2tbCAtFIi/MlgZmuYd8jYGEQK3r3qgtMS3NrExPXJFnEBEzA+14O/rgGhQwcOb5eGNBk9Ei3V2DRjDvyCF8oqoPT9XwlLdREmj7LT3/pm0KHXh7f/uE6xeYcBP1xFvfKy6sTmboyviPz4dKXjALWP+cL1VZLitjQkzf4SAxAglvEJZ/2qYndBgLfibsmwGUbffywVSJ9CgUmtxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAM88Uw0WXmBQtAyCAp4EtcS4aty1Ap94USKsgWwa7IidGZA77oDKw+w1VcdCWUDBsGwOmXvkW2KFyPD3lXURb1LeKitU1orzldme5/hSbRHUlE3YZ4x2lxGrRdERFhAurI6Ci7eRyL4Bsuya4sV5SxFOK0wp1IKdlStAlbpwExYs=".getBytes();
            RSAPublicKey  key = null;
            try {
                key = (RSAPublicKey ) getPublicKey();
                System.out.println(key);
            }   catch (CertificateException e) {
                e.printStackTrace();
            }

            String accessToken = "eyJraWQiOiJPSURDV2ViR2F0ZURvbWFpbjYiLCJ4NXQiOiJ4enBJNjBQWmZqSE5bEVJaFA3LWVzcFNVYlkiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbWZ0Y3FhMTZvaHMudGVtZW5vc2dyb3VwLmNvbTo0MDIwMS9vYXV0aDIiLCJhdWQiOltdLCJleHAiOjE2MDE5OTM3NzEsImp0aSI6InJQLTY3cHp6TVlmcHMxXzVneS1wQXciLCJpYXQiOjE2MDE5OTAxNzEsInN1YiI6Im1mbGRhZG0iLCJzZXNzaW9uQ291bnQiOiJOVUxMIiwic2Vzc2lvbkNyZWF0aW9uIjoiTlVMTCIsImN1c3RvbUF0dHIxIjoiQ3VzdG9tVmFsdWUiLCJTdGF0aWNBdHRyIjoiU3RhdGljQXR0clZhbHVlIiwic2Vzc2lvbklkIjoiQ09PS0lFX0JBU0VEIiwibWZ1c2VyZXhwaXJ5ZGF0ZSI6Ik5PVF9GT1VORCIsInVzZXJleHBpcnlkYXRlIjoiTlVMTCIsInVzZXJJZCI6Im1mbGRhZG0iLCJ1c2VyR3JvdXAiOiJNRkFETUlOIiwiY2xpZW50IjoiT0lEQ1dlYkdhdGVDbGllbnQ2Iiwic2NvcGUiOlsib3BlbmlkIl0sImRvbWFpbiI6Ik9JRENXZWJHYXRlRG9tYWluNiJ9.x2YeyscdSP5Bx2AP8sJtUklE9Z3Qh3vRi8u39KvjMQ3nq8uBahd2ASSkm2TYI4byc5IKCiPcThMVPBVXK1yIKFjr1u2TVBiX2cHJhKhymA7PigPORDDajlD2NBtDqPFOYk3qfXoXa86uhTfBpPkyQj0MZ72RawdlSbRL3UtPZTVND_xNOvGsSAvCqWRyq5IDZMASLlDfQUWKlsAUBIGEJPct5oELzO80C7GtbevG3hG9YN_pVOTj3ovkUUTikrOImgNW7C1qHAC04zvyZhKtEcRuEwj7ZbORDeC7l_CAdI6KuVah_r6oZqiK4DnVmjr8B4zOAobLAhtrn5iuNjznIg";
            DecodedJWT decodedJwt = JWT.decode(accessToken);

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) key, null);
            try{
                Verification verifier = JWT.require(algorithm);
                verifier.build().verify(decodedJwt);
            } catch (JWTVerificationException exception){
                //Invalid signature/claims
                System.out.println("failde to verfy" + exception.getMessage());
            }
        }
    }


