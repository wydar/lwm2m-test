package com.example;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import static org.eclipse.leshan.core.LwM2mId.*;
import static org.eclipse.leshan.client.object.Security.*;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.core.request.BindingMode;
/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // Load model
        List<ObjectModel> models = ObjectLoader.loadDefault();
        String[] modelPaths = new String[] {"Test_ACL_2.xml"};
        models.addAll(ObjectLoader.loadDdfResources("/modelos", modelPaths));

        // Create initializer with your models
        ObjectsInitializer initializer = new ObjectsInitializer(new StaticModel(models));
        // Create `LwM2mInstanceEnabler` instances.
        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec("leshan.eclipseprojects.io", 123));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(123, 30, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new Device("Manufacturer", "modelNumber", "serialNumber", BindingMode.U.name()));
        initializer.setInstancesForObject(10242, new Your10242InstanceEnabler());
        // Use it to create LWM2MObjectEnabler
        List<LwM2mObjectEnabler> enablers = initializer.createAll();



        String endpoint = "leshan.eclipseprojects.io"; // choose an endpoint name
        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
        LeshanClient client = builder.build();
        client.start();
    }
}
