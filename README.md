# jmxdumper
A simple utility for dumping all of the jmx beans and attributes into a yml file for use with New Relic's nri-jmx on-host integration

## Build
jmxdumper uses Maven for generating the binaries:

```bash
$ mvn package
```

This will create the `jmxdumper.jar` file under the `./bin/` directory. 

Note: jmxdumper is targetted to work with Java 7

## Usage
The applicaton just expects the connection parameters to the JMX interface.

```bash
paulbrant$ java -jar bin/jmxpicker.jar -h
usage: nrjmx
 -d,--debug                                       Debug mode
 -H,--hostname <arg>                              JMX hostname (localhost)
 -h,--help                                        Show help
 -keyStore,--keyStore <arg>                       SSL keyStore location
 -keyStorePassword,--keyStorePassword <arg>       SSL keyStorePassword
 -P,--port <arg>                                  JMX port (7199)
 -p,--password <arg>                              JMX password
 -r,--remote                                      Remote JMX mode
 -trustStore,--trustStore <arg>                   SSL trustStore location
 -trustStorePassword,--trustStorePassword <arg>   SSL trustStorePassword
 -U,--uriPath <arg>                               path for the JMX service
                                                  URI. Defaults to jmxrmi
 -u,--username <arg>                              JMX username
 -v,--verbose                                     Verbose output
```

The utility connects to the java application via a JMX connection and iterates through MBeans. Based on the data it retrieves, it dumps the domains, mbeans, and attriutes to stdout in yml format. Log messages are written to stderr.

Example usage:

```bash
$ java -jar bin/jmxpicker.jar -P 1099 -verbose > jmx.yml 2>stderr.txt
```

Warning: The utility does NOT validate that values will be valid for use with New Relic's nri-jmx OHI. For example, the event\_type is just the domain with the word Sample appended. Many JMX domains contain characters which are illegal in the event\_type. You may also end up with a query that does not have any attributes.

It is NOT recommended to use all of the beans and attributes produced by running this utility. The intention of the utility is to make it easier to pull the data that is valuable. Much of the data is not needed so the intention is that the configuration produced be edited down to meaningful values.


