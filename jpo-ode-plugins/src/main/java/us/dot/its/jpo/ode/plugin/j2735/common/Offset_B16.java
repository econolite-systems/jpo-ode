package us.dot.its.jpo.ode.plugin.j2735.common;

import us.dot.its.jpo.ode.plugin.types.Asn1Integer;
import com.fasterxml.jackson.annotation.JsonCreator;
import us.dot.its.jpo.ode.plugin.serialization.IntegerDeserializer;

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
public class Offset_B16 extends Asn1Integer {

	public Offset_B16() {
		super(-32768L, 32767L);
	}

	@JsonCreator
	public Offset_B16(long value) {
		this();
		this.value = value;
	}

	public static class Offset_B16Deserializer extends IntegerDeserializer<Offset_B16> {
		public Offset_B16Deserializer() {
			super(Offset_B16.class);
		}

		@Override
		protected Offset_B16 construct() {
			return new Offset_B16();
		}
	}
}