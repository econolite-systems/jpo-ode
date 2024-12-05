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
public class OffsetLL_B24 extends Asn1Integer {

	public OffsetLL_B24() {
		super(-8388608L, 8388607L);
	}

	@JsonCreator
	public OffsetLL_B24(long value) {
		this();
		this.value = value;
	}

	public static class OffsetLL_B24Deserializer extends IntegerDeserializer<OffsetLL_B24> {
		public OffsetLL_B24Deserializer() {
			super(OffsetLL_B24.class);
		}

		@Override
		protected OffsetLL_B24 construct() {
			return new OffsetLL_B24();
		}
	}
}