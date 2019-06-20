package org.newrelic.nrjmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class JMXFetcher {
    private static final Logger logger = Logger.getLogger("nrjmx");

    private MBeanServerConnection connection;

    public class ConnectionError extends Exception {
        public ConnectionError(String message, Exception cause) {
            super(message, cause);
        }
    }


    public JMXFetcher(String hostname, int port, String uriPath, String username, String password , String keyStore, String keyStorePassword, String trustStore, String trustStorePassword, boolean isRemote) throws ConnectionError {
        String connectionString = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/%s", hostname, port, uriPath);
        if (isRemote) {
            connectionString = String.format("service:jmx:remoting-jmx://%s:%s", hostname, port);
        }

        Map<String, Object> env = new HashMap<>();
        if (!"".equals(username)) {
            env.put(JMXConnector.CREDENTIALS, new String[]{username, password});
        }

        if (!"".equals(keyStore) && !"".equals(trustStore)) {
            Properties p = System.getProperties();
            p.put("javax.net.ssl.keyStore", keyStore);
            p.put("javax.net.ssl.keyStorePassword", keyStorePassword);
            p.put("javax.net.ssl.trustStore", trustStore);
            p.put("javax.net.ssl.trustStorePassword", trustStorePassword);
            env.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
        }

        try {
            JMXServiceURL address = new JMXServiceURL(connectionString);
            JMXConnector connector = JMXConnectorFactory.connect(address, env);
            connection = connector.getMBeanServerConnection();
        } catch (IOException e) {
            throw new ConnectionError("Can't connect to JMX server: " + connectionString, e);
        }
    }

    public void processMBeans() throws Exception {
    	System.out.println("collect:");
    	for (String domain : connection.getDomains()) {
    		System.out.println("  - domain: " + domain);
    		System.out.println("    event_type: " + domain + "Sample");
			System.out.println("    beans:");
    		Set<ObjectName> mb = connection.queryNames(new ObjectName(domain + ":*"), null);
    		for (final ObjectName mbean: mb) {
    			String canonicalName = mbean.getCanonicalName();
    			String mbeanName = canonicalName.substring(domain.length() + 1, canonicalName.length());
    			System.out.println("      - query: " + mbeanName);
    			System.out.println("        attributes:");
    			final MBeanAttributeInfo[] attributes = connection.getMBeanInfo(mbean).getAttributes();
    			for (final MBeanAttributeInfo attribute : attributes) {
    				if (attribute == null) {
    					continue;
    				}
    				if (!attribute.isReadable()) {
        				logger.fine("Attribute: " + attribute.getName() + " is not readable");    					
    					continue;
    				}
    				
    				String attType = attribute.getType();
    				if (attType == null) {
						logger.fine("Attribute type is null: " + attribute.getName());
						continue;
    				}
    				switch (attType) {
					case "int":
					case "long":
					case "double":
		    			System.out.println("        - attr: " + attribute.getName());
		    			System.out.println("          metric_type: " + "gauge");
						break;
						
					case "java.lang.String":
					case "boolean":
		    			System.out.println("        - attr: " + attribute.getName());
		    			System.out.println("          metric_type: " + "attribute");
						break;

					case "javax.management.openmbean.CompositeData":
		    			System.out.println("        - " + attribute.getName());
						break;
						
					default:
						logger.fine("Attribute type not supported: " + attribute.getName() + " type: " + attType);
						break;
					}
    			}
    		}
    	}
    }
}
    
