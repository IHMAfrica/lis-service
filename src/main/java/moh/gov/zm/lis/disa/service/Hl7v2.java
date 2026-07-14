package moh.gov.zm.lis.disa.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Hl7v2 {
   private final Map<String, List<String[]>> segments = new LinkedHashMap();

   private Hl7v2() {
   }

   static Hl7v2 parse(String raw) {
      Hl7v2 msg = new Hl7v2();
      if (raw == null) {
         return msg;
      } else {
         for(String line : raw.split("[\r\n]+")) {
            if (!line.isBlank()) {
               String[] tokens = line.split("\\|", -1);
               String name = tokens[0];
               String[] fields;
               if ("MSH".equals(name)) {
                  fields = new String[tokens.length + 1];
                  fields[0] = name;
                  fields[1] = "|";

                  for(int i = 1; i < tokens.length; ++i) {
                     fields[i + 1] = tokens[i];
                  }
               } else {
                  fields = tokens;
               }

               ((List)msg.segments.computeIfAbsent(name, (k) -> new ArrayList())).add(fields);
            }
         }

         return msg;
      }
   }

   boolean has(String segment) {
      return this.segments.containsKey(segment);
   }

   List<String[]> all(String segment) {
      return (List)this.segments.getOrDefault(segment, List.of());
   }

   String field(String segment, int fieldNo) {
      List<String[]> reps = (List)this.segments.get(segment);
      return reps != null && !reps.isEmpty() ? field((String[])reps.getFirst(), fieldNo) : "";
   }

   static String field(String[] fields, int fieldNo) {
      return fields != null && fieldNo >= 0 && fieldNo < fields.length && fields[fieldNo] != null ? fields[fieldNo].trim() : "";
   }

   static String component(String value, int componentNo) {
      if (value != null && !value.isEmpty()) {
         String[] parts = value.split("\\^", -1);
         return componentNo >= 1 && componentNo <= parts.length ? parts[componentNo - 1].trim() : "";
      } else {
         return "";
      }
   }

   static String unescape(String value) {
      return value != null && value.indexOf(92) >= 0 ? value.replace("\\F\\", "|").replace("\\S\\", "^").replace("\\R\\", "~").replace("\\T\\", "&").replace("\\E\\", "\\") : value;
   }
}
