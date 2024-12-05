package us.dot.its.jpo.ode.plugin.j2735.region;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import us.dot.its.jpo.ode.plugin.j2735.common.RegionalExtension;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import us.dot.its.jpo.ode.plugin.annotations.Asn1ParameterizedTypes;
import us.dot.its.jpo.ode.plugin.annotations.Asn1ParameterizedTypes.IdType;
import us.dot.its.jpo.ode.plugin.j2735.addgrpb.NodeOffsetPointXY_addGrpBReg_NodeOffsetPointXY;

/**
 * 
 *******************************************************************************
 *
 * This source file was generated by a tool. Beware manual edits might be
 * overwritten in future releases. asn1jvm v1.0-SNAPSHOT
 *
 *******************************************************************************
 * Copyright 2024 USDOT
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************
 * 
 */
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "regionId")
@JsonSubTypes({
		@JsonSubTypes.Type(value = NodeOffsetPointXY_addGrpBReg_NodeOffsetPointXY.class, name = "2")})
@Asn1ParameterizedTypes(idProperty = "regionId", idType = IdType.INTEGER, valueProperty = "regExtValue", value = {
		@Asn1ParameterizedTypes.Type(value = NodeOffsetPointXY_addGrpBReg_NodeOffsetPointXY.class, intId = 2)})
abstract public class Reg_NodeOffsetPointXY<TValue> extends RegionalExtension<TValue> {

	public Reg_NodeOffsetPointXY(int id, String name) {
		super(id, name);
	}
}