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

package com.yunmei.frame.servlet.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A &quot;factory&quot; for producing ClassData information from Class objects.
 * Gathers the ClassData information via reflection and internally caches it.
 */
public class ClassAnalyzer {
	private final static Logger log = Logger.getLogger(ClassAnalyzer.class);

	private static Map classCache = new HashMap();

	public static ClassData getClassData(Class clazz) {
		ClassData cd;
		synchronized (classCache) {
			cd = (ClassData) classCache.get(clazz);
			if (cd == null) {
				cd = analyzeClass(clazz);
				classCache.put(clazz, cd);
			}
		}
		return cd;
	}

	/**
	 * Empty the internal cache of ClassData information.
	 */
	public static void invalidateCache() {
		classCache = new HashMap();
	}

	private static ClassData analyzeClass(Class clazz) {
		final List constructors = new ArrayList(Arrays.asList(clazz
				.getConstructors()));
		final List memberMethods = new ArrayList();
		final List staticMethods = new ArrayList();
		{
			final Method methods[] = clazz.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (Modifier.isStatic(methods[i].getModifiers())) {
					staticMethods.add(methods[i]);
				} else {
					memberMethods.add(methods[i]);
				}
			}
		}
		//TODO 不知道为啥，哈哈
		ClassData cd = new ClassData(clazz, createMap(memberMethods, false),
				createMap(staticMethods, false), createMap(constructors, true));
		return cd;
	}

	/**
	 * Creates a mapping of AccessibleObjectKey to a Collection which contains
	 * all the AccessibleObjects which have the same amount of arguments. This
	 * takes into account LocalArgResolvers, discounting them from the argument
	 * size.
	 * 
	 * @param accessibleObjects
	 *            The objects to put into the map
	 * @param isConstructor
	 *            Whether the objects are methods or constructors
	 * @return Map of AccessibleObjectKey to a Collection of AccessibleObjects
	 */
	@SuppressWarnings("unchecked")
	private static Map createMap(Collection accessibleObjects,
			boolean isConstructor) {
		final Map map = new HashMap();
		for (final Iterator i = accessibleObjects.iterator(); i.hasNext();) {
			final Member accessibleObject = (Member) i.next();

			if (!Modifier.isPublic(accessibleObject.getModifiers()))
				continue;

			final AccessibleObjectKey accessibleObjectKey;
			{
				// argCount determines the key
				int argCount = 0;
				{
					// The parameters determine the size of argCount
					final Class[] param;
					if (isConstructor) {
						param = ((Constructor) accessibleObject)
								.getParameterTypes();
					} else {
						// If it is a method and the method was defined in
						// Object(), skip
						// it.
						if (((Method) accessibleObject).getDeclaringClass() == Object.class) {
							continue;
						}
						param = ((Method) accessibleObject).getParameterTypes();
					}
					if (isConstructor) {
						accessibleObjectKey = new AccessibleObjectKey(
								"$constructor", 0);
					} else {
						accessibleObjectKey = new AccessibleObjectKey(
								((Method) accessibleObject).getName(),
								param.length);
					}
				}
			}
			List marr = (ArrayList) map.get(accessibleObjectKey);
			if (marr == null) {
				marr = new ArrayList();
				map.put(accessibleObjectKey, marr);
			}

			marr.add(accessibleObject);
		}
		return map;
	}
}
