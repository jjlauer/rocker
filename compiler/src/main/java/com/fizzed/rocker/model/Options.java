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
import com.fizzed.rocker.compiler.ParserException;
import com.fizzed.rocker.compiler.TokenException;
import java.nio.charset.Charset;

/**
 *
 * @author joelauer
 */
public class Options {
    
    static public final String JAVA_VERSION = "javaVersion";
    static public final String DISCARD_LOGIC_WHITESPACE = "discardLogicWhitespace";
    static public final String COMBINE_ADJACENT_PLAIN = "combineAdjacentPlain";
    static public final String EXTENDS_CLASS = "extendsClass";
    static public final String IMPLEMENTS_INTERFACE = "implementsInterface";
    static public final String TARGET_CHARSET = "targetCharset";
    
    // generated source compatiblity
    private JavaVersion javaVersion;
    // discard lines consisting of only logic/block
    private Boolean discardLogicWhitespace;
    // combine adjacent plain text elements together to form a single one
    // almost should never be disabled -- much more efficient templates
    private Boolean combineAdjacentPlain;
    // parent class of template
    private String extendsClass;
    // interface template implements
    private String implementsInterface;
    // target charset template will render with
    private String targetCharset;
    
    public Options() {
        this.javaVersion = JavaVersion.v1_8;
        this.discardLogicWhitespace = null;                 // will default to default of content type
        this.combineAdjacentPlain = Boolean.TRUE;
        this.extendsClass = com.fizzed.rocker.runtime.DefaultRockerTemplate.class.getName();
        this.implementsInterface = null;
        this.targetCharset = "UTF-8";
    }
    
    public Options copy() {
        Options options = new Options();
        options.javaVersion = this.javaVersion;
        options.discardLogicWhitespace = this.discardLogicWhitespace;
        options.combineAdjacentPlain = this.combineAdjacentPlain;
        options.extendsClass = this.extendsClass;
        options.implementsInterface = this.implementsInterface;
        options.targetCharset = this.targetCharset;
        return options;
    }

    public JavaVersion getJavaVersion() {
        return javaVersion;
    }
    
    public boolean isGreaterThanOrEqualToJavaVersion(JavaVersion javaVersion) {
        return this.javaVersion.getVersion() >= javaVersion.getVersion();
    }

    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    public void setJavaVersion(String javaVersion) throws TokenException {
        if (javaVersion == null) {
            throw new TokenException("javaVersion was null");
        }
        
        JavaVersion jv = JavaVersion.findByLabel(javaVersion);
        if (jv == null) {
            throw new TokenException("Unsupported javaVersion [" + javaVersion + "]");
        }
        
        this.javaVersion = jv;
    }

    public Boolean getDiscardLogicWhitespace() {
        return discardLogicWhitespace;
    }

    public boolean getDiscardLogicWhitespaceForContentType(ContentType type) {
        if (this.discardLogicWhitespace == null) {
            return ContentType.discardLogicWhitespace(type);
        } else {
            return discardLogicWhitespace;
        }
    }
    
    public void setDiscardLogicWhitespace(Boolean discardLogicWhitespace) {
        this.discardLogicWhitespace = discardLogicWhitespace;
    }

    public Boolean getCombineAdjacentPlain() {
        return combineAdjacentPlain;
    }

    public void setCombineAdjacentPlain(Boolean combineAdjacentPlain) {
        this.combineAdjacentPlain = combineAdjacentPlain;
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    public String getImplementsInterface() {
        return implementsInterface;
    }

    public void setImplementsInterface(String implementsInterface) {
        this.implementsInterface = implementsInterface;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(String targetCharset) {
        // verify this charset exists... (will throw unchecked exception)
        Charset.forName(targetCharset);
        
        this.targetCharset = targetCharset;
    }

    public void set(String name, String value) throws TokenException {
        String optionName = name.trim();
        String optionValue = value.trim();
        
        switch (optionName) {
            case DISCARD_LOGIC_WHITESPACE:
                this.setDiscardLogicWhitespace(parseBoolean(optionValue));
                break;
            case COMBINE_ADJACENT_PLAIN:
                this.setCombineAdjacentPlain(parseBoolean(optionValue));
                break;
            case JAVA_VERSION:
                this.setJavaVersion(optionValue);
                break;
            case EXTENDS_CLASS:
                this.setExtendsClass(optionValue);
                break;
            case IMPLEMENTS_INTERFACE:
                this.setImplementsInterface(optionValue);
                break;
            case TARGET_CHARSET:
                this.setTargetCharset(optionValue);
                break;
            default:
                throw new TokenException("Invalid option (" + optionName + ") is not a property)");
        }
    }
    
    public void parseOption(Option option) throws ParserException {
        String statement = option.getStatement();
        if (!statement.contains("=")) {
            throw new ParserException(option.getSourceRef(), null, "Invalid option (missing = token; format name=value)");
        }
        
        String[] nameValuePair = statement.split("=");
        if (nameValuePair == null || nameValuePair.length != 2) {
            throw new ParserException(option.getSourceRef(), null, "Invalid option (must have only a single = token)");
        }
        
        try {
            set(nameValuePair[0], nameValuePair[1]);
        } catch (TokenException e) {
            throw new ParserException(option.getSourceRef(), null, e.getMessage(), e);
        }
    }
    
    private Boolean parseBoolean(String value) throws TokenException {
        if (value == null) {
            throw new TokenException("Boolean option cannot be null");
        }
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        else if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        else {
            throw new TokenException("Unparseable boolean");
        }
    }

}
