package us.dot.its.jpo.ode.plugin.j2735.travelerinformation;

import us.dot.its.jpo.ode.plugin.j2735.common.Position3D;
import us.dot.its.jpo.ode.plugin.types.Asn1Sequence;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import us.dot.its.jpo.ode.plugin.annotations.Asn1Property;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Circle extends Asn1Sequence {

	@Asn1Property(tag = 0)
	private Position3D center;
	@Asn1Property(tag = 1)
	@JsonDeserialize(using = Radius_B12.Radius_B12Deserializer.class)
	private Radius_B12 radius;
	@Asn1Property(tag = 2)
	private DistanceUnits units;

	Circle() {
		super(false);
	}
}