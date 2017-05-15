/*
  Copyright 1995-2017 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */

package com.esri.geoevent.transport.azure;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.GeoEventAwareTransport;
import com.esri.ges.transport.OutboundTransportBase;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.util.Validator;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ServiceClient;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AzureDeviceOutboundTransport extends OutboundTransportBase implements GeoEventAwareTransport {
  // logger
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AzureDeviceOutboundTransport.class);

  // connection properties
  private String connectionString = "";
  private String deviceIdGedName = "";
  private String deviceIdFieldName = "";

  private volatile boolean propertiesNeedUpdating = false;

  // device id client and receiver
  private static ServiceClient serviceClient = null;
  private static FeedbackReceiver feedbackReceiver = null;

  public AzureDeviceOutboundTransport(TransportDefinition definition) throws ComponentException {
    super(definition);
  }

  @Override
  public synchronized void start() {
    switch (getRunningState()) {
      case STARTING:
      case STARTED:
        return;
      default:
    }

    setRunningState(RunningState.STARTING);
    setup();
  }

  public void readProperties() {
    try {
      boolean somethingChanged = false;

      // Connection String
      if (hasProperty(AzureDeviceOutboundTransportDefinition.CONNECTION_STRING_PROPERTY_NAME)) {
        String newConnectionString = getProperty(AzureDeviceOutboundTransportDefinition.CONNECTION_STRING_PROPERTY_NAME).getValueAsString();
        if (!connectionString.equals(newConnectionString)) {
          connectionString = newConnectionString;
          somethingChanged = true;
        }
      }
      // Device Id GED Name
      if (hasProperty(AzureDeviceOutboundTransportDefinition.DEVICE_ID_GED_NAME_PROPERTY_NAME)) {
        String newGEDName = getProperty(AzureDeviceOutboundTransportDefinition.DEVICE_ID_GED_NAME_PROPERTY_NAME).getValueAsString();
        if (!deviceIdGedName.equals(newGEDName)) {
          deviceIdGedName = newGEDName;
          somethingChanged = true;
        }
      }
      // Device Id Field Name
      if (hasProperty(AzureDeviceOutboundTransportDefinition.DEVICE_ID_FIELD_NAME_PROPERTY_NAME)) {
        String newDeviceIdFieldName = getProperty(AzureDeviceOutboundTransportDefinition.DEVICE_ID_FIELD_NAME_PROPERTY_NAME).getValueAsString();
        if (!deviceIdFieldName.equals(newDeviceIdFieldName)) {
          deviceIdFieldName = newDeviceIdFieldName;
          somethingChanged = true;
        }
      }
      propertiesNeedUpdating = somethingChanged;
    } catch (Exception ex) {
      LOGGER.error("INIT_ERROR", ex.getMessage());
      LOGGER.info(ex.getMessage(), ex);
      setErrorMessage(ex.getMessage());
      setRunningState(RunningState.ERROR);
    }
  }

  public synchronized void setup() {
    String errorMessage = null;
    RunningState runningState = RunningState.STARTED;

    try {
      readProperties();
      if (propertiesNeedUpdating) {
        cleanup();
        propertiesNeedUpdating = false;
      }

      // setup Azure IoT Device
      serviceClient = ServiceClient.createFromConnectionString(connectionString, IotHubServiceClientProtocol.AMQPS);
      serviceClient.open();

      // feedbackReceiver = serviceClient.getFeedbackReceiver(deviceId);
      // if (feedbackReceiver == null)
      // {
      // // TODO: error messages
      // throw new RuntimeException("ERROR");
      // }
      // feedbackReceiver.open();

      setErrorMessage(errorMessage);
      setRunningState(runningState);
    } catch (Exception ex) {
      LOGGER.error("INIT_ERROR", ex.getMessage());
      LOGGER.info(ex.getMessage(), ex);
      setErrorMessage(ex.getMessage());
      setRunningState(RunningState.ERROR);
    }
  }

  protected void cleanup() {
    // clean up the service client
    if (serviceClient != null) {
      try {
        serviceClient.close();
      } catch (Exception error) {
        ;
      }
    }

    // clean up the receiver
    if (feedbackReceiver != null) {
      try {
        feedbackReceiver.close();
      } catch (Exception error) {
        ;
      }
    }
  }

  @Override
  public void receive(ByteBuffer buffer, String channelId) {
    receive(buffer, channelId, null);
  }

  @Override
  public void receive(ByteBuffer buffer, String channelId, GeoEvent geoEvent) {
    if (isRunning()) {
      if (geoEvent == null)
        return;

      try {
        // Send Event to a Device
        Object deviceIdObj = geoEvent.getField(deviceIdFieldName);
        String deviceId = "";
        if (deviceIdObj != null)
          deviceId = deviceIdObj.toString();

        if (Validator.isNotBlank(deviceId)) {
          String messageStr = new String(buffer.array(), StandardCharsets.UTF_8);
          Message message = new Message(messageStr);
          serviceClient.sendAsync(deviceId, message);

          // receive feedback from the device
          // FeedbackBatch feedback = feedbackReceiver.receive(10000);
          // feedback.toString();
        } else {
          LOGGER.warn("FAILED_TO_SEND_INVALID_DEVICE_ID", deviceIdFieldName);
        }
      } catch (Exception e) {
        // streamClient.stop();
        setErrorMessage(e.getMessage());
        LOGGER.error(e.getMessage(), e);
        setRunningState(RunningState.ERROR);
      }
    } else {
      LOGGER.debug("RECEIVED_BUFFER_WHEN_STOPPED", "");
    }
  }

}
