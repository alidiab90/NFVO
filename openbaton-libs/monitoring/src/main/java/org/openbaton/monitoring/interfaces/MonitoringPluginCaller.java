package org.openbaton.monitoring.interfaces;

import com.google.gson.reflect.TypeToken;
import org.openbaton.catalogue.mano.common.monitoring.*;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.plugin.utils.PluginCaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lto on 25/11/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.rabbit")
public class MonitoringPluginCaller extends MonitoringPlugin {

    public String getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(String managementPort) {
        this.managementPort = managementPort;
    }

    @Value("${nfvo.rabbit.management.port:}")
    private String  managementPort;

    public MonitoringPluginCaller(String type,String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type, "localhost", "admin", "openbaton", 5672, Integer.parseInt(managementPort));
    }

    public MonitoringPluginCaller(String name, String type,String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type +"."+name, "localhost", "admin", "openbaton", 5672, Integer.parseInt(managementPort));
    }

    public MonitoringPluginCaller(String brokerIp, int port, String type,String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor." + type, brokerIp, "admin", "openbaton", port, Integer.parseInt(managementPort));
    }

    public MonitoringPluginCaller(String brokerIp, String username, String password, String type,String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("monitor."+ type, brokerIp, username, password, 5672, Integer.parseInt(managementPort));
    }

    private PluginCaller pluginCaller;

    public void stop() throws Exception {
        pluginCaller.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException, NotFoundException, ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        class Exec implements Callable<Object>{

            @Override
            public Object call() throws Exception {
                List<String> hosts = new ArrayList<>();
                hosts.add("hostname1");
                hosts.add("hostname2");
                return null/* new MonitoringPluginCaller("zabbix").getMeasurementResults(hosts, new ArrayList<String>(), "")*/;
            }
        }

        long time = new Date().getTime();
        System.out.println("Time ---> " + time);
        Future fut1 = executor.submit(new Exec());
        Future fut2 = executor.submit(new Exec());

        System.out.println("2nd call");
        System.out.println(fut2.get());

        System.out.println("1st call");
        System.out.println(fut1.get());
        System.out.println("Time ---> " + (new Date().getTime() - time)/1000);

    }

    @Override
    public String subscribeForFault(AlarmEndpoint filter) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(filter);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("subscribeForFault", params, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public String unsubscribeForFault(String alarmEndpointId) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(alarmEndpointId);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("unsubscribeForFault", params, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public void notifyFault(AlarmEndpoint endpoint, AbstractVirtualizedResourceAlarm event) throws MonitoringException {
        //It must not be called from a consumer
    }

    @Override
    public List<Alarm> getAlarmList(String vnfId, PerceivedSeverity perceivedSeverity) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(vnfId);
        params.add(perceivedSeverity);

        Serializable res = null;
        try {
            Type listType = new TypeToken<ArrayList<Alarm>>() { }.getType();
            res = pluginCaller.executeRPC("getAlarmList", params, listType);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<Alarm>) res;
    }

    @Override
    public String createPMJob(ObjectSelection resourceSelector, List<String> performanceMetric, List<String> performanceMetricGroup, Integer collectionPeriod, Integer reportingPeriod) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(resourceSelector);
        params.add((Serializable) performanceMetric);
        params.add((Serializable) performanceMetricGroup);
        params.add(collectionPeriod);
        params.add(reportingPeriod);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("createPMJob", params, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public List<String> deletePMJob(List<String> itemIdsToDelete) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable)itemIdsToDelete);

        Serializable res = null;
        Type listType = new TypeToken<ArrayList<String>>() { }.getType();
        try {
            res = pluginCaller.executeRPC("deletePMJob", params, listType);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<String>) res;
    }

    @Override
    public List<Item> queryPMJob(List<String> hostnames, List<String> metrics, String period) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable) hostnames);
        params.add((Serializable) metrics);
        params.add(period);
        Serializable res = null;
        try {

            Type listType = new TypeToken<ArrayList<Item>>() { }.getType();
            res = pluginCaller.executeRPC("queryPMJob", params, listType);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<Item>) res;
    }

    @Override
    public void subscribe() {
        //not yet implemented
    }

    @Override
    public void notifyInfo() {
        //not yet implemented
    }

    @Override
    public String createThreshold(ObjectSelection objectSelector, String performanceMetric, ThresholdType thresholdType, ThresholdDetails thresholdDetails) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add(objectSelector);
        params.add(performanceMetric);
        params.add(thresholdType);
        params.add(thresholdDetails);

        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("createThreshold", params, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (String) res;
    }

    @Override
    public List<String> deleteThreshold(List<String> thresholdIds) throws MonitoringException {
        List<Serializable> params = new ArrayList<>();
        params.add((Serializable)thresholdIds);

        Serializable res = null;
        try {
            Type listType = new TypeToken<ArrayList<String>>() { }.getType();
            res = pluginCaller.executeRPC("deleteThreshold", params, listType);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (PluginException e) {
            throw new MonitoringException(e.getMessage());
        }
        return (List<String>) res;
    }

    @Override
    public void queryThreshold(String queryFilter) {
        //not yet implemented
    }
}