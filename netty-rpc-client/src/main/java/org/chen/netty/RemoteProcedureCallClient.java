package org.chen.netty;

import com.adamo.service.dto.Request;
import com.adamo.service.dto.Response;

import java.util.List;
import java.util.Map;

public interface RemoteProcedureCallClient {

    void updateServiceNameChannel(Map<String, List<String>> map);

    Response send(Request request, String serviceName) throws Exception;
}
