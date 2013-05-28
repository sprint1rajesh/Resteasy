package org.jboss.resteasy.validation;

import java.io.Serializable;

import org.jboss.resteasy.spi.validation.ConstraintType;

public class ResteasyConstraintViolation implements Serializable
{
   private static final long serialVersionUID = -5441628046215135260L;
   
   private ConstraintType.Type constraintType;
   private String path;
   private String message;
   private String value;
   
   public ResteasyConstraintViolation(ConstraintType.Type constraintType, String path, String message, String value)
   {
      this.constraintType = constraintType;
      this.path = path;
      this.message = message;
      this.value = value;
   }
   
   public ConstraintType.Type getConstraintType()
   {
      return constraintType;
   }
   
   public String getPath()
   {
      return path;
   }
   public String getMessage()
   {
      return message;
   }
   public String getValue()
   {
      return value;
   }
   public String toString()
   {
      return type() + "| " + path + "| " + message + "| " + value;
   }
   public String type()
   {
      return constraintType.toString();
   }
}