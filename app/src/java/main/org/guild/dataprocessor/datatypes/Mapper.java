package org.guild.dataprocessor.datatypes;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Mapper {

    private static final Logger LOG = LoggerFactory.getLogger(Mapper.class);

    private Map<Integer, Metadata> dataMap = new HashMap<>();

    public void addItem(Metadata obj) {
        dataMap.put(obj.getId(), obj);
    }

    public Metadata getItem(Integer id) {
        if (dataMap.containsKey(id)) return dataMap.get(id);
        else {
            LOG.warn("Movie was not found by that id: " + id);
            return null;
        }
    }

    public JSONArray toJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<Integer, Metadata> obj: dataMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonArray.put(obj.getKey(), obj.getValue().getJson());
        }
        return jsonArray;
    }
}
