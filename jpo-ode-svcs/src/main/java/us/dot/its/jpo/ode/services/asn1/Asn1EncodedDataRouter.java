/*******************************************************************************
 * Copyright 2018 572682
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package us.dot.its.jpo.ode.services.asn1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.dot.its.jpo.ode.OdeProperties;
import us.dot.its.jpo.ode.context.AppContext;
import us.dot.its.jpo.ode.eventlog.EventLogger;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.services.asn1.Asn1CommandManager.Asn1CommandManagerException;
import us.dot.its.jpo.ode.traveler.TimTransmogrifier;
import us.dot.its.jpo.ode.util.CodecUtils;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.JsonUtils.JsonUtilsException;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.wrapper.AbstractSubscriberProcessor;
import us.dot.its.jpo.ode.wrapper.MessageProducer;

public class Asn1EncodedDataRouter extends AbstractSubscriberProcessor<String, String> {

   private static final String BYTES = "bytes";

  private static final String MESSAGE_FRAME = "MessageFrame";

  private static final String ERROR_ON_DDS_DEPOSIT = "Error on DDS deposit.";

  public static class Asn1EncodedDataRouterException extends Exception {

      private static final long serialVersionUID = 1L;

      public Asn1EncodedDataRouterException(String string) {
         super(string);
      }

   }

   private Logger logger = LoggerFactory.getLogger(this.getClass());

   private OdeProperties odeProperties;
   private MessageProducer<String, String> stringMsgProducer;
   private Asn1CommandManager asn1CommandManager;

   public Asn1EncodedDataRouter(OdeProperties odeProperties) {
      super();

      this.odeProperties = odeProperties;

      this.stringMsgProducer = MessageProducer.defaultStringMessageProducer(odeProperties.getKafkaBrokers(),
            odeProperties.getKafkaProducerType(), this.odeProperties.getKafkaTopicsDisabledSet());

      this.asn1CommandManager = new Asn1CommandManager(odeProperties);

   }

   @Override
   public Object process(String consumedData) {
      try {
         logger.debug("Consumed: {}", consumedData);
         JSONObject consumedObj = XmlUtils.toJSONObject(consumedData).getJSONObject(OdeAsn1Data.class.getSimpleName());

         /*
          * When receiving the 'rsus' in xml, since there is only one 'rsu' and
          * there is no construct for array in xml, the rsus does not translate
          * to an array of 1 element. The following workaround, resolves this
          * issue.
          */
         JSONObject metadata = consumedObj.getJSONObject(AppContext.METADATA_STRING);

         if (metadata.has(TimTransmogrifier.REQUEST_STRING)) {
            JSONObject request = metadata.getJSONObject(TimTransmogrifier.REQUEST_STRING);

            if (request.has(TimTransmogrifier.RSUS_STRING)) {
               JSONObject rsusIn = (JSONObject) request.get(TimTransmogrifier.RSUS_STRING);
               if (rsusIn.has(TimTransmogrifier.RSUS_STRING)) {
                 Object rsu = rsusIn.get(TimTransmogrifier.RSUS_STRING);
                 JSONArray rsusOut = new JSONArray();
                 if (rsu instanceof JSONArray) {
                   logger.debug("Multiple RSUs exist in the request: {}", request);
                   JSONArray rsusInArray = (JSONArray) rsu;
                   for (int i = 0; i < rsusInArray.length(); i++) {
                     rsusOut.put(rsusInArray.get(i));
                   }
                   request.put(TimTransmogrifier.RSUS_STRING, rsusOut);
                 } else if (rsu instanceof JSONObject) {
                   logger.debug("Single RSU exists in the request: {}", request);
                   rsusOut.put(rsu);
                   request.put(TimTransmogrifier.RSUS_STRING, rsusOut);
                 } else {
                   logger.debug("No RSUs exist in the request: {}", request);
                   request.remove(TimTransmogrifier.RSUS_STRING);
                 }
               }
            }

            // Convert JSON to POJO
            ServiceRequest servicerequest = getServicerequest(consumedObj);

            processEncodedTim(servicerequest, consumedObj);
         } else {
            throw new Asn1EncodedDataRouterException("Invalid or missing '"
                + TimTransmogrifier.REQUEST_STRING + "' object in the encoder response");
         }
      } catch (Exception e) {
         String msg = "Error in processing received message from ASN.1 Encoder module: " + consumedData;
         EventLogger.logger.error(msg, e);
         logger.error(msg, e);
      }
      return null;
   }

   public ServiceRequest getServicerequest(JSONObject consumedObj) {
      String sr = consumedObj.getJSONObject(AppContext.METADATA_STRING).getJSONObject(TimTransmogrifier.REQUEST_STRING).toString();
      logger.debug("ServiceRequest: {}", sr);

      // Convert JSON to POJO
      ServiceRequest serviceRequest = null;
      try {
         serviceRequest = (ServiceRequest) JsonUtils.fromJson(sr, ServiceRequest.class);

      } catch (Exception e) {
         String errMsg = "Malformed JSON.";
         EventLogger.logger.error(errMsg, e);
         logger.error(errMsg, e);
      }

      return serviceRequest;
   }

   public void processEncodedTim(ServiceRequest request, JSONObject consumedObj) {

      JSONObject dataObj = consumedObj.getJSONObject(AppContext.PAYLOAD_STRING).getJSONObject(AppContext.DATA_STRING);

      // CASE 1: no SDW in metadata (SNMP deposit only)
      // - sign MF
      // - send to RSU
      // CASE 2: SDW in metadata but no ASD in body (send back for another
      // encoding)
      // - sign MF
      // - send to RSU
      // - craft ASD object
      // - publish back to encoder stream
      // CASE 3: If SDW in metadata and ASD in body (double encoding complete)
      // - send to DDS

      if (!dataObj.has(Asn1CommandManager.ADVISORY_SITUATION_DATA_STRING)) {
         logger.debug("Unsigned message received");
         // We don't have ASD, therefore it must be just a MessageFrame that needs to be signed
         // No support for unsecured MessageFrame only payload.
         // Cases 1 & 2
         // Sign and send to RSUs

         JSONObject mfObj = dataObj.getJSONObject(MESSAGE_FRAME);

         String hexEncodedTim = mfObj.getString(BYTES);
         logger.debug("Encoded message - phase 1: {}", hexEncodedTim);
         //use Asnc1 library to decode the encoded tim returned from ASNC1; another class two blockers: decode the tim and decode the message-sign

         if (odeProperties.dataSigningEnabled()) {
            logger.debug("Sending message for signature! ");
            String base64EncodedTim = CodecUtils.toBase64(
               CodecUtils.fromHex(hexEncodedTim));
            JSONObject matadataObjs = consumedObj.getJSONObject(AppContext.METADATA_STRING);
            // get max duration time and convert from minutes to milliseconds (unsigned
				// integer valid 0 to 2^32-1 in units of
				// milliseconds.) from metadata
            int maxDurationTime = Integer.valueOf(matadataObjs.get("maxDurationTime").toString()) * 60 * 1000;
				String timpacketID = matadataObjs.getString("odePacketID");
				String timStartDateTime = matadataObjs.getString("odeTimStartDateTime");
            String signedResponse = asn1CommandManager.sendForSignature(base64EncodedTim,maxDurationTime);
            try {
               hexEncodedTim = CodecUtils.toHex(
                  CodecUtils.fromBase64(
                     JsonUtils.toJSONObject(JsonUtils.toJSONObject(signedResponse).getString("result")).getString("message-signed")));
                     
               JSONObject TimWithExpiration = new JSONObject();
					TimWithExpiration.put("packetID", timpacketID);
					TimWithExpiration.put("startDateTime", timStartDateTime);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					try {
						JSONObject jsonResult = JsonUtils
								.toJSONObject((JsonUtils.toJSONObject(signedResponse).getString("result")));
						// messageExpiry uses unit of seconds
						long messageExpiry = Long.valueOf(jsonResult.getString("message-expiry"));
						TimWithExpiration.put("expirationDate", dateFormat.format(new Date(messageExpiry * 1000)));
					} catch (Exception e) {
						logger.error("Unable to get expiration date from signed messages response {}", e);
            TimWithExpiration.put("expirationDate", "null");
					}

					try {
						Date parsedtimTimeStamp = dateFormat.parse(timStartDateTime);
						Date requiredExpirationDate = new Date();
						requiredExpirationDate.setTime(parsedtimTimeStamp.getTime() + maxDurationTime);
						TimWithExpiration.put("requiredExpirationDate", dateFormat.format(requiredExpirationDate));
					} catch (Exception e) {
						logger.error("Unable to parse requiredExpirationDate {}", e);
            TimWithExpiration.put("requiredExpirationDate", "null");
					}
					//publish to Tim expiration kafka 
					stringMsgProducer.send(odeProperties.getKafkaTopicSignedOdeTimJsonExpiration(), null,
							TimWithExpiration.toString());

            } catch (JsonUtilsException e1) {
               logger.error("Unable to parse signed message response {}", e1);
            }
         }
         else {
            // if 001F is not first 4 bytes, then strip 1609.2 header from unsigned message
            if (!hexEncodedTim.substring(0, 4).equals("001F")) {
               logger.debug("Stripping 1609.2 header from unsigned message");
               hexEncodedTim = strip1609Dot2Header(hexEncodedTim);
            }
         }

         if (null != request.getSnmp() && null != request.getRsus() && null != hexEncodedTim) {
            logger.info("Sending message to RSUs...");
            asn1CommandManager.sendToRsus(request, hexEncodedTim);           
         }

         if (request.getSdw() != null) {
            // Case 2 only

            logger.debug("Publishing message for round 2 encoding!");
            String xmlizedMessage = asn1CommandManager.packageSignedTimIntoAsd(request, hexEncodedTim);

            stringMsgProducer.send(odeProperties.getKafkaTopicAsn1EncoderInput(), null, xmlizedMessage);
         }

      } else {
         //We have encoded ASD. It could be either UNSECURED or secured.
         logger.debug("securitySvcsSignatureUri = {}", odeProperties.getSecuritySvcsSignatureUri());

         if (odeProperties.dataSigningEnabled()) {
            logger.debug("Signed message received. Depositing it to SDW.");
            // We have a ASD with signed MessageFrame
            // Case 3
            JSONObject asdObj = dataObj.getJSONObject(Asn1CommandManager.ADVISORY_SITUATION_DATA_STRING);
            try {
              asn1CommandManager.depositToSdw(asdObj.getString(BYTES));
            } catch (JSONException | Asn1CommandManagerException e) {
              String msg = ERROR_ON_DDS_DEPOSIT;
              logger.error(msg, e);
            }
         } else {
            logger.debug("Unsigned ASD received. Depositing it to SDW.");
            //We have ASD with UNSECURED MessageFrame
            processEncodedTimUnsecured(request, consumedObj);
         }
      }
   }

   public void processEncodedTimUnsecured(ServiceRequest request, JSONObject consumedObj) {
      // Send TIMs and record results
      HashMap<String, String> responseList = new HashMap<>();

      JSONObject dataObj = consumedObj
            .getJSONObject(AppContext.PAYLOAD_STRING)
            .getJSONObject(AppContext.DATA_STRING);

      if (null != request.getSdw()) {
         JSONObject asdObj = null;
         if (dataObj.has(Asn1CommandManager.ADVISORY_SITUATION_DATA_STRING)) {
            asdObj = dataObj.getJSONObject(Asn1CommandManager.ADVISORY_SITUATION_DATA_STRING);
         } else {
            logger.error("ASD structure present in metadata but not in JSONObject!");
         }

        if (null != asdObj) {
           String asdBytes = asdObj.getString(BYTES);

           // Deposit to DDS
           String ddsMessage = "";
           try {
              asn1CommandManager.depositToSdw(asdBytes);
              ddsMessage = "\"dds_deposit\":{\"success\":\"true\"}";
              logger.info("DDS deposit successful.");
           } catch (Exception e) {
              ddsMessage = "\"dds_deposit\":{\"success\":\"false\"}";
              String msg = ERROR_ON_DDS_DEPOSIT;
              logger.error(msg, e);
              EventLogger.logger.error(msg, e);
           }

           responseList.put("ddsMessage", ddsMessage);
        } else if (logger.isErrorEnabled()) { // Added to avoid Sonar's "Invoke method(s) only conditionally." code smell
          String msg = "ASN.1 Encoder did not return ASD encoding {}";
          EventLogger.logger.error(msg, consumedObj.toString());
          logger.error(msg, consumedObj.toString());
        }
      }

      if (dataObj.has(MESSAGE_FRAME)) {
         JSONObject mfObj = dataObj.getJSONObject(MESSAGE_FRAME);
         String encodedTim = mfObj.getString(BYTES);

         // if 001F is not first 4 bytes, then strip 1609.2 header from unsigned message
         if (!encodedTim.substring(0, 4).equals("001F")) {
            logger.debug("Stripping 1609.2 header from unsigned message");
            encodedTim = strip1609Dot2Header(encodedTim);
            mfObj.put(BYTES, encodedTim);
         }

         logger.debug("Encoded message - phase 2: {}", encodedTim);

        // only send message to rsu if snmp, rsus, and message frame fields are present
        if (null != request.getSnmp() && null != request.getRsus() && null != encodedTim) {
           logger.debug("Encoded message phase 3: {}", encodedTim);
           asn1CommandManager.sendToRsus(request, encodedTim);           
         }
      }

      logger.info("TIM deposit response {}", responseList);
   }

   /**
    * Strips 1609.2 header from unsigned message
    */
   private String strip1609Dot2Header(String encodedUnsignedTim) {
      String toReturn = "";
      // find 001F hex value
      int index = encodedUnsignedTim.indexOf("001F");
      if (index == -1) {
         logger.warn("No '001F' hex value found in encoded message");
         return encodedUnsignedTim;
      }
      // strip everything before 001F
      toReturn = encodedUnsignedTim.substring(index);
      return toReturn;
   }
}
