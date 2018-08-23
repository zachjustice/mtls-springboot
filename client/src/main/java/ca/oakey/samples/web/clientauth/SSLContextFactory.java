package ca.oakey.samples.web.clientauth;

import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

@Component
public class SSLContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(SSLContextFactory.class);

    private String keystoreFileName;
    private Map<String, String> certs;

    @Autowired
    public SSLContextFactory(
            @Value("${keystore}") String keystoreFileName
    ){
        this.keystoreFileName = keystoreFileName;
        Properties props = new Properties();
        try {
            props.load(SSLContextFactory.class.getResourceAsStream("/certificates.properties"));
        } catch (IOException e) {
            throw new RuntimeException("loading certificates", e);
        }
        certs = props.entrySet().stream()
                .map(e -> Pair.of((String) e.getKey(), (String) e.getValue()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    public SSLContext createSSLContext() throws Exception {
        char[] password = "Newclient02".toCharArray();

        KeyStore keyStore = createKeyStore();

        return SSLContextBuilder
                .create()
                // NOTE sending the wrong cert/providing the wrong keystore should get a `SSLHandshakeException: Received fatal alert: bad_certificate"`
                // NOTE aliasStrategy that just returns the `usom-tax` alias gives a 234001 error -- no username presented
                .loadKeyMaterial(loadPfx("/" + keystoreFileName, password), password,  (a, b) -> "usom-tax")
                // NOTE commenting loadTrustMaterial out will get a `ValidatorException: PKIX path building failed` error
                // NOTE not sure how to add .cer to certificate.properties for truststore
                // NOTE can't use the usom-tax-keystore for truststore. Will get the PKIX ValidatorException error
                .loadTrustMaterial(keyStore, null) // use for thd version
//                 .loadTrustMaterial(loadPfx("/" + keystoreFileName, password), null) // use for the self-signed version
                .build();
    }

    private KeyStore loadPfx(String file, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream in = getClass().getResourceAsStream(file);
        keyStore.load(in, password);
        return keyStore;
    }

    private TrustManagerFactory createTrustManagerFactory(KeyStore ks) throws NoSuchAlgorithmException,
            KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        return tmf;
    }

    private KeyStore createKeyStore() throws GeneralSecurityException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return appendCertificates(certs.entrySet().stream().map(cert -> {
            try {
                ByteArrayInputStream certBais = new ByteArrayInputStream(cert.getValue().getBytes());
                return Pair.of(cert.getKey(), (X509Certificate) cf.generateCertificate(certBais));
            } catch (CertificateException e) {
                throw new IllegalStateException(e);
            }
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
    }

    private KeyStore appendCertificates(Map<String, Certificate> certificates) {
        try {
            X509TrustManager trustManager = getDefaultTrustManager();

            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null);

                if (trustManager != null) {
                    for (X509Certificate cert : trustManager.getAcceptedIssuers()) {
                        trustStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
                        logger.debug("adding existing certificate to truststore {}", cert.getSubjectDN().getName());
                    }
                }

                certificates.forEach((alias, certificate) -> {
                    try {
                        trustStore.setCertificateEntry(alias, certificate);
                        logger.debug("adding new certificate to truststore {}", alias);
                    } catch (SecurityException | KeyStoreException ex) {
                        logger.error("unable to add certificate: {}", certificate);
                        throw new IllegalStateException(ex);
                    }
                });

                return trustStore;
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private X509TrustManager getDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory factory = createTrustManagerFactory((KeyStore) null);
        TrustManager[] trustManagers = factory.getTrustManagers();
        logger.debug("TrustManagers = {}");
        return (X509TrustManager) trustManagers[0];
    }
}
