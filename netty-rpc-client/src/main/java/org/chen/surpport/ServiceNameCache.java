package org.chen.surpport;

import java.util.ArrayList;
import java.util.List;

public class ServiceNameCache {

    private static List<String> serviceNames = new ArrayList<>();

    public static List<String> getServiceNames(){
        return serviceNames;
    }

    public static boolean addServiceName(String serviceName){
        return serviceNames.add(serviceName);
    }

    public static void clear(){
        serviceNames.clear();
    }
}
