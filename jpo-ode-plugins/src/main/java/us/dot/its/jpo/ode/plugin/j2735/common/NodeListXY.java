package us.dot.its.jpo.ode.plugin.j2735.common;

import us.dot.its.jpo.ode.plugin.types.Asn1Choice;
import lombok.Getter;
import lombok.Setter;
import us.dot.its.jpo.ode.plugin.annotations.Asn1Property;
import java.util.Optional;
import java.util.List;
import us.dot.its.jpo.ode.plugin.types.Asn1Type;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

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
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class NodeListXY extends Asn1Choice {

	@Asn1Property(tag = 0)
	@JacksonXmlElementWrapper(localName = "nodes")
	@JacksonXmlProperty(localName = "NodeXY")
	private NodeSetXY nodes;
	@Asn1Property(tag = 1)
	private ComputedLane computed;

	NodeListXY() {
		super(true);
	}

	@Override
	protected List<Optional<Asn1Type>> listTypes() {
		return null;
	}
}