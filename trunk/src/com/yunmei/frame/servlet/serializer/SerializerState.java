/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.yunmei.frame.servlet.serializer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yunmei.frame.exception.MarshallException;
import com.yunmei.frame.exception.UnmarshallException;
import com.yunmei.frame.servlet.reflect.ProcessedObject;

/**
 * Used by Serializers to hold state during marshalling and
 * unmarshalling.  It keeps track of all Objects encountered
 * during processing for the purpose of detecting circular
 * references and/or duplicates.
 */
public class SerializerState
{
  private Map processedObjects = new IdentityHashMap();
  private LinkedList currentLocation = new LinkedList();
  
  public ProcessedObject getProcessedObject(Object object)
  {
    // get unique key for this object
    // this is the basis for determining if we have already processed the object or not.
    return (ProcessedObject) processedObjects.get(object);
  }

  public boolean isAncestor(ProcessedObject dup, Object parent)
  {
    ProcessedObject ancestor = getProcessedObject(parent);
    while (ancestor != null)
    {
      if (dup == ancestor)
      {
        return true;
      }
      ancestor = ancestor.getParent();
    }
    return false;
  }

  public void pop() throws MarshallException
  {
    if (currentLocation.size()==0)
    {
      // this is a sanity check
      throw new MarshallException("scope error, attempt to pop too much off the scope stack.");
    }
    currentLocation.removeLast();
  }

  public void push(Object parent, Object obj, Object ref)
  {
    ProcessedObject parentProcessedObject = null;

    if (parent!=null)
    {
      parentProcessedObject = getProcessedObject(parent);

      if (parentProcessedObject==null)
      {
        // this is a sanity check-- it should never occur
        throw new IllegalArgumentException("attempt to process an object with an unprocessed parent");
      }
    }

    ProcessedObject p = new ProcessedObject();
    p.setParent(parentProcessedObject);
    p.setObject(obj);

    processedObjects.put(obj, p);
    if (ref != null)
    {
      p.setRef(ref);
      currentLocation.add(ref);
    }
  }

  public void setSerialized(Object source, Object target) throws UnmarshallException
  {
    if (source==null)
    {
      throw new UnmarshallException("source object may not be null");
    }
    ProcessedObject p = getProcessedObject(source);
    if (p == null)
    {
      throw new UnmarshallException("source object must be already registered as a ProcessedObject " + source);
    }
    p.setSerialized(target);
  }

  public ProcessedObject store(Object obj)
  {
    ProcessedObject p = new ProcessedObject();
    p.setObject(obj);

    processedObjects.put(obj, p);
    return p;
  }
}
