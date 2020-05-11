/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2020 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.jackson;

import java.io.IOException;

import com.adobe.cq.wcm.core.components.models.datalayer.ComponentDataModel;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ComponentDataModelSerializer extends StdSerializer<ComponentDataModel> {

    public ComponentDataModelSerializer() {
        this(null);
    }

    public ComponentDataModelSerializer(Class<ComponentDataModel> t) {
        super(t);
    }

    @Override
    public void serialize(ComponentDataModel componentDataModel, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.setCodec(new ObjectMapper());
        jsonGenerator.writeObjectField(componentDataModel.getId(), componentDataModel);
        jsonGenerator.writeEndObject();
    }
}