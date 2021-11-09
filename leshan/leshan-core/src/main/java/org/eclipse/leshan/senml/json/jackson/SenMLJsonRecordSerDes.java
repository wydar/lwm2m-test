/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Boya Zhang - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.senml.json.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.util.Base64;
import org.eclipse.leshan.core.util.datatype.ULong;
import org.eclipse.leshan.core.util.json.JacksonJsonSerDes;
import org.eclipse.leshan.core.util.json.JsonException;
import org.eclipse.leshan.senml.SenMLRecord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SenMLJsonRecordSerDes extends JacksonJsonSerDes<SenMLRecord> {
    private boolean allowNoValue;

    public SenMLJsonRecordSerDes() {
        this(false);
    }

    /**
     * Create SenML-JSON serializer/deserializer based on Jackson.
     * <p>
     * SenML value is defined as mandatory in <a href="https://tools.ietf.org/html/rfc8428#section-4.2">rfc8428</a>, but
     * SenML records used with a Read-Composite operation do not contain any value field, so
     * <code>allowNoValue=true</code> can be used skip this validation.
     * 
     * @param allowNoValue <code>True</code> to not check if there is a value for each SenML record.
     */
    public SenMLJsonRecordSerDes(boolean allowNoValue) {
        this.allowNoValue = allowNoValue;
    }

    @Override
    public JsonNode jSerialize(SenMLRecord record) throws JsonException {
        ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();

        if (record.getBaseName() != null && record.getBaseName().length() > 0) {
            jsonObj.put("bn", record.getBaseName());
        }

        if (record.getBaseTime() != null) {
            jsonObj.put("bt", record.getBaseTime());
        }

        if (record.getName() != null && record.getName().length() > 0) {
            jsonObj.put("n", record.getName());
        }

        if (record.getTime() != null) {
            jsonObj.put("t", record.getTime());
        }

        Type type = record.getType();
        if (type != null) {
            switch (record.getType()) {
            case FLOAT:
                Number value = record.getFloatValue();
                // integer
                if (value instanceof Byte) {
                    jsonObj.put("v", value.byteValue());
                } else if (value instanceof Short) {
                    jsonObj.put("v", value.shortValue());
                } else if (value instanceof Integer) {
                    jsonObj.put("v", value.intValue());
                } else if (value instanceof Long) {
                    jsonObj.put("v", value.longValue());
                } else if (value instanceof BigInteger) {
                    jsonObj.put("v", (BigInteger) value);
                }
                // unsigned integer
                else if (value instanceof ULong) {
                    jsonObj.put("v", ((ULong) value).toBigInteger());
                }
                // floating-point
                else if (value instanceof Float) {
                    jsonObj.put("v", value.floatValue());
                } else if (value instanceof Double) {
                    jsonObj.put("v", value.doubleValue());
                } else if (value instanceof BigDecimal) {
                    jsonObj.put("v", (BigDecimal) value);
                }
                break;
            case BOOLEAN:
                jsonObj.put("vb", record.getBooleanValue());
                break;
            case OBJLNK:
                jsonObj.put("vlo", record.getObjectLinkValue());
                break;
            case OPAQUE:
                jsonObj.put("vd", Base64.encodeBase64String(record.getOpaqueValue()));
                break;
            case STRING:
                jsonObj.put("vs", record.getStringValue());
                break;
            default:
                break;
            }
        } else {
            if (!allowNoValue)
                throw new JsonException("Invalid SenML record : record must have a value (v,vb,vlo,vd,vs) : %s",
                        record);
        }
        return jsonObj;
    }

    @Override
    public SenMLRecord deserialize(JsonNode o) throws JsonException {
        if (o == null)
            return null;

        SenMLRecord record = new SenMLRecord();

        JsonNode bn = o.get("bn");
        if (bn != null && bn.isTextual())
            record.setBaseName(bn.asText());

        JsonNode bt = o.get("bt");
        if (bt != null && bt.isNumber())
            record.setBaseTime(bt.asLong());

        JsonNode n = o.get("n");
        if (n != null && n.isTextual())
            record.setName(n.asText());

        JsonNode t = o.get("t");
        if (t != null && t.isNumber())
            record.setTime(t.asLong());

        JsonNode v = o.get("v");
        boolean hasValue = false;
        if (v != null && v.isNumber()) {
            record.setFloatValue(v.numberValue());
            hasValue = true;
        }

        JsonNode vb = o.get("vb");
        if (vb != null && vb.isBoolean()) {
            record.setBooleanValue(vb.asBoolean());
            hasValue = true;
        }

        JsonNode vs = o.get("vs");
        if (vs != null && vs.isTextual()) {
            record.setStringValue(vs.asText());
            hasValue = true;
        }

        JsonNode vlo = o.get("vlo");
        if (vlo != null && vlo.isTextual()) {
            record.setObjectLinkValue(vlo.asText());
            hasValue = true;
        }

        JsonNode vd = o.get("vd");
        if (vd != null && vd.isTextual()) {
            record.setOpaqueValue(Base64.decodeBase64(vd.asText()));
            hasValue = true;
        }

        if (!allowNoValue && !hasValue)
            throw new JsonException("Invalid SenML record : record must have a value (v,vb,vlo,vd,vs) : %s", o);

        return record;
    }
}
