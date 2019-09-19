package org.bouncycastle.jsse.provider;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.Set;

import org.bouncycastle.jsse.java.security.BCAlgorithmConstraints;
import org.bouncycastle.jsse.java.security.BCCryptoPrimitive;

class ProvAlgorithmConstraints
    extends AbstractAlgorithmConstraints
{
    private static final String PROPERTY_CERTPATH_DISABLED_ALGORITHMS = "jdk.certpath.disabledAlgorithms";
    private static final String PROPERTY_TLS_DISABLED_ALGORITHMS = "jdk.tls.disabledAlgorithms";

    private static final DisabledAlgorithmConstraints provTlsDisabledAlgorithms =
        DisabledAlgorithmConstraints.create(ProvAlgorithmDecomposer.INSTANCE_TLS, PROPERTY_TLS_DISABLED_ALGORITHMS);
    private static final DisabledAlgorithmConstraints provX509DisabledAlgorithms =
        DisabledAlgorithmConstraints.create(ProvAlgorithmDecomposer.INSTANCE_X509, PROPERTY_CERTPATH_DISABLED_ALGORITHMS);

    static final ProvAlgorithmConstraints DEFAULT = new ProvAlgorithmConstraints(null, true);
    static final ProvAlgorithmConstraints DEFAULT_TLS_ONLY = new ProvAlgorithmConstraints(null, false);

    private final BCAlgorithmConstraints configAlgorithmConstraints;
    private final Set<String> supportedSignatureAlgorithms;
    private final boolean enableX509Constraints;

    public ProvAlgorithmConstraints(BCAlgorithmConstraints configAlgorithmConstraints, boolean enableX509Constraints)
    {
        super(null);

        this.configAlgorithmConstraints = configAlgorithmConstraints;
        this.supportedSignatureAlgorithms = null;
        this.enableX509Constraints = enableX509Constraints;
    }

    public ProvAlgorithmConstraints(BCAlgorithmConstraints configAlgorithmConstraints,
        String[] supportedSignatureAlgorithms, boolean enableX509Constraints)
    {
        super(null);

        this.configAlgorithmConstraints = configAlgorithmConstraints;
        this.supportedSignatureAlgorithms = asUnmodifiableSet(supportedSignatureAlgorithms);
        this.enableX509Constraints = enableX509Constraints;
    }

    public boolean permits(Set<BCCryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters)
    {
        checkPrimitives(primitives);
        checkAlgorithmName(algorithm);

        if (null != supportedSignatureAlgorithms && !isSupportedSignatureAlgorithm(algorithm))
        {
            return false;
        }

        if (null != configAlgorithmConstraints && !configAlgorithmConstraints.permits(primitives, algorithm, parameters))
        {
            return false;
        }

        if (null != provTlsDisabledAlgorithms && !provTlsDisabledAlgorithms.permits(primitives, algorithm, parameters))
        {
            return false;
        }

        if (enableX509Constraints && null != provX509DisabledAlgorithms
            && !provX509DisabledAlgorithms.permits(primitives, algorithm, parameters))
        {
            return false;
        }

        return true;
    }

    public boolean permits(Set<BCCryptoPrimitive> primitives, Key key)
    {
        checkPrimitives(primitives);
        checkKey(key);

        if (null != configAlgorithmConstraints && !configAlgorithmConstraints.permits(primitives, key))
        {
            return false;
        }

        if (null != provTlsDisabledAlgorithms && !provTlsDisabledAlgorithms.permits(primitives, key))
        {
            return false;
        }

        if (enableX509Constraints && null != provX509DisabledAlgorithms
            && !provX509DisabledAlgorithms.permits(primitives, key))
        {
            return false;
        }

        return true;
    }

    public boolean permits(Set<BCCryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters)
    {
        checkPrimitives(primitives);
        checkAlgorithmName(algorithm);
        checkKey(key);

        if (null != supportedSignatureAlgorithms && !isSupportedSignatureAlgorithm(algorithm))
        {
            return false;
        }

        if (null != configAlgorithmConstraints && !configAlgorithmConstraints.permits(primitives, algorithm, key, parameters))
        {
            return false;
        }

        if (null != provTlsDisabledAlgorithms && !provTlsDisabledAlgorithms.permits(primitives, algorithm, key, parameters))
        {
            return false;
        }

        if (enableX509Constraints && null != provX509DisabledAlgorithms
            && !provX509DisabledAlgorithms.permits(primitives, algorithm, key, parameters))
        {
            return false;
        }

        return true;
    }

    private boolean isSupportedSignatureAlgorithm(String algorithm)
    {
        return !supportedSignatureAlgorithms.isEmpty()
            && containsIgnoreCase(supportedSignatureAlgorithms, removeAnyMGFSpecifier(algorithm));
    }

    private static String removeAnyMGFSpecifier(String algorithm)
    {
        // TODO[jsse] Follows SunJSSE behaviour. Case-insensitive search?
        int andPos = algorithm.indexOf("and");
        return andPos < 1 ? algorithm : algorithm.substring(0, andPos);
    }
}
