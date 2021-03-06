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

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.TransportDefinitionBase;
import com.esri.ges.transport.TransportType;

public class AzureEventHubOutboundTransportDefinition extends TransportDefinitionBase {
  // logger
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AzureEventHubOutboundTransportDefinition.class);

  // property names
  public static final String CONNECTION_STRING_PROPERTY_NAME = "connectionString";

  public AzureEventHubOutboundTransportDefinition() {
    super(TransportType.OUTBOUND);
    try {
      propertyDefinitions.put(CONNECTION_STRING_PROPERTY_NAME, new PropertyDefinition(CONNECTION_STRING_PROPERTY_NAME, PropertyType.String, null, "${com.esri.geoevent.transport.azure-event-hub-transport.CONNECTION_STR_LBL}", "${com.esri.geoevent.transport.azure-event-hub-transport.CONNECTION_STR_DESC}", true, false));
    } catch (PropertyException error) {
      LOGGER.error("ERROR_LOADING_TRANSPORT_DEFINITION", error);
      throw new RuntimeException(error);
    }
  }

  @Override
  public String getName() {
    return "azure-event-hub-out";
  }

  @Override
  public String getLabel() {
    return "${com.esri.geoevent.transport.azure-event-hub-transport.TRANSPORT_OUT_LABEL}";
  }

  @Override
  public String getDomain() {
    return "com.esri.geoevent.transport.outbound";
  }

  @Override
  public String getDescription() {
    return "${com.esri.geoevent.transport.azure-event-hub-transport.TRANSPORT_OUT_DESC}";
  }
  
  @Override
  public String getVersion() {
    return "10.6.0";
  }
}
