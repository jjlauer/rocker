/*
 * Copyright 2015 Fizzed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.rocker.model;

import com.fizzed.rocker.ContentType;
import static com.fizzed.rocker.compiler.JavaGenerator.CRLF;
import com.fizzed.rocker.compiler.RockerUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author joelauer
 */
public class TemplateModel {
    
    // e.g. "views.system"
    private final String packageName;
    // e.g. "index.rocker.html"
    private final String templateName;
    private final ContentType contentType;
    // e.g. "index"
    private final String name;
    private final List<JavaImport> imports;
    private final List<Argument> arguments;
    private final List<TemplateUnit> units;
    private final Options options;
    
    public TemplateModel(String packageName, String templateName, Options defaultOptions) {
        this.packageName = packageName;
        this.templateName = templateName;
        this.name = RockerUtil.templateNameToName(templateName);
        this.contentType = RockerUtil.templateNameToContentType(templateName);
        this.imports = new ArrayList<>();
        this.arguments = new ArrayList<>();
        this.units = new ArrayList<>();
        this.options = defaultOptions;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public List<JavaImport> getImports() {
        return imports;
    }

    public List<Argument> getArguments() {
        return arguments;
    }
    
    public boolean hasRockerBodyArgument() {
        return getRockerBodyArgument() != null;
    }
    
    public Argument getRockerBodyArgument() {
        if (!arguments.isEmpty()) {
            
            Argument lastArgument = arguments.get(arguments.size() - 1);

            if (lastArgument.getType().equals("RockerBody")) {
                return lastArgument;
            }
        }
        return null;
    }
    
    public List<Argument> getArgumentsWithoutRockerBody() {
        if (hasRockerBodyArgument()) {
            return arguments.subList(0, arguments.size() - 1);
        } else {
            return arguments;
        }
    }

    public Options getOptions() {
        return options;
    }

    public List<TemplateUnit> getUnits() {
        return units;
    }
    
    public <T extends TemplateUnit> T getUnit(int index, Class<T> type) {
        return (T)units.get(index);
    }
    
    public LinkedHashMap<String,LinkedHashMap<String,String>> createPlainTextMap(int chunkSize) {
        
        LinkedHashMap<String, LinkedHashMap<String,String>> plainTextMap = new LinkedHashMap<>();
        
        // optimize static plain text constants
        int index = 0;
        plainTextMap = new LinkedHashMap<>();

        for (TemplateUnit unit : getUnits()) {
            if (unit instanceof PlainText) {
                PlainText plain = (PlainText)unit;

                if (!plainTextMap.containsKey(plain.getText())) {
                    
                    LinkedHashMap<String,String> chunkMap = new LinkedHashMap<>();
                    plainTextMap.put(plain.getText(), chunkMap);
                    
                    // split text into chunks
                    List<String> chunks = RockerUtil.stringIntoChunks(plain.getText(), chunkSize);
                    
                    for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {

                        String varName = "PLAIN_TEXT_" + index + "_" + chunkIndex;
                        
                        String chunkText = chunks.get(chunkIndex);

                        chunkMap.put(varName, chunkText);
                    }

                    index++;
                }
            }
        }

        return plainTextMap;
    }
    
}
