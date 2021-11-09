package org.eclipse.leshan.server.core.demo.cli;

import java.io.File;

import org.eclipse.leshan.core.LwM2m;
import org.eclipse.leshan.core.demo.cli.converters.PortConverter;

import picocli.CommandLine.Option;

/**
 * Main options shared by bsserver-demo and server-demo
 */
public class GeneralSection {

    @Option(names = { "-lh", "--coap-host" },
            description = { //
                    "Set the local CoAP address of the Server.", //
                    "Default: any local address." })
    public String localAddress;

    @Option(names = { "-lp", "--coap-port" },
            description = { //
                    "Set the local CoAP port of the Server.", //
                    "Default: COAP_PORT value from 'Californium.properties' file or ${DEFAULT-VALUE} if absent" },
            converter = PortConverter.class)
    public Integer localPort = LwM2m.DEFAULT_COAP_PORT;

    @Option(names = { "-slh", "--coaps-host" },
            description = { //
                    "Set the secure local CoAP address of the Server.", //
                    "Default: any local address." })
    public String secureLocalAddress;

    @Option(names = { "-slp", "--coaps-port" },
            description = { //
                    "Set the secure local CoAP port of the Server.", //
                    "Default: COAP_SECURE_PORT value from 'Californium.properties' file or ${DEFAULT-VALUE} if absent" },
            converter = PortConverter.class)
    public Integer secureLocalPort = LwM2m.DEFAULT_COAP_SECURE_PORT;

    @Option(names = { "-wh", "--web-host" },
            description = { //
                    "Set the HTTP address for web server.", //
                    "Default: any local address." })
    public String webhost;

    @Option(names = { "-wp", "--web-port" },
            defaultValue = "8080",
            description = { //
                    "Set the HTTP port for web server.", //
                    "Default: ${DEFAULT-VALUE}" },
            converter = PortConverter.class)
    public Integer webPort;

    @Option(names = { "-m", "--models-folder" },
            description = { //
                    "A folder which contains object models in OMA DDF(xml)format." })
    public File modelsFolder;
}
