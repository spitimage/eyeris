/*
 * Copyright (C) 2010 keystoremanager authors
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

package com.blooco.eyeris;


public class Asn1Protocol 
{
	public byte tag;
	public Object value;

	//define some asn1 tags
	public static final byte RAW_DATA = 0;
	
	public static final byte SEQUENCE = 0x30;
	public static final byte OPTIONAL = (byte)0xa0;
	public static final byte INTEGER = 0x02;
	public static final byte OBJECT_ID = 0x06;
	public static final byte SET = 0x31;
	public static final byte UTC_TIME = 0x17;
	public static final byte PRINTABLE_STRING = 0x13;
	public static final byte NULL = 0x05;
	public static final byte BIT_STRING = 0x03;
	
	public static final byte[] RSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01 }; // 1.2.840.113549.1.1.1

	public static final byte[] sha1RSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x05 }; // 1.2.840.113549.1.1.5 

	public static final byte[] sha1DSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0xce, (byte)0x38, (byte)0x04, (byte)0x03 }; // 1.2.840.10040.4.3 

	public static final byte[] country = { (byte)0x55, (byte)0x04, (byte)0x06 }; // 2.5.4.6 Pays/r�gion (C)
	public static final byte[] commonName = { (byte)0x55, (byte)0x04, (byte)0x03 }; // 2.5.4.3 Nom commun (CN)
	public static final byte[] state = { (byte)0x55, (byte)0x04, (byte)0x08 }; // 2.5.4.8 D�partement ou province (S)
	public static final byte[] locality = { (byte)0x55, (byte)0x04, (byte)0x07 }; // 2.5.4.7 Ville (L)
	public static final byte[] organization = { (byte)0x55, (byte)0x04, (byte)0x0a }; // 2.5.4.10 Organisation (O)
	public static final byte[] organizationUnit = { (byte)0x55, (byte)0x04, (byte)0x0b }; // 2.5.4.11 Unit� d'organisation Unit (OU)

	public Asn1Protocol(byte tag, Object value)
	{
		this.tag = tag;
		this.value = value;
	}

	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}
	
	public static String byteToHex(byte[] buf) 
	{ 
			String s;
			StringBuffer strBuffer = new StringBuffer();
			
			for (int i = 0; i < buf.length; i++) {
					s = Integer.toHexString(unsignedByteToInt(buf[i]));
					if(s.length() < 2)
						strBuffer.append('0');
					strBuffer.append(s);
			  }
			  return strBuffer.toString();
	}

	private static byte[] asn1_add(byte[] in, byte type, byte[] bd)
	{
		byte[] tag;
		
		// ugly hack but...
		if(type == BIT_STRING)
		{
			byte[] t = new byte[bd.length+1];
			t[0] = 0;
			System.arraycopy(bd, 0, t, 1, bd.length);
			bd = t;
		}
		else if(type == RAW_DATA)
		{
			return append(in,bd);
		}
		
		if(bd.length > 255)
		{
			// TAG 82 XX XX 
			tag = new byte[4];
			tag[0] = type;
			tag[1] = (byte)0x82;
			tag[2] = (byte)((bd.length/256)&0xff);
			tag[3] = (byte)((bd.length%256)&0xff);
		}
		else if(bd.length > 127)
		{
			// TAG 81 XX  
			tag = new byte[3];
			tag[0] = type;
			tag[1] = (byte)0x81;
			tag[2] = (byte)((bd.length%256)&0xff);
		}
		else
		{
			// TAG 81 XX  
			tag = new byte[2];
			tag[0] = type;
			tag[1] = (byte)((bd.length%256)&0xff);
		}
		byte[] out = new byte[in.length + tag.length + bd.length];
		
		System.arraycopy(in, 0, out, 0, in.length);
		System.arraycopy(tag, 0, out, in.length, tag.length);
		System.arraycopy(bd, 0, out, in.length+tag.length, bd.length);
		
		return out;
	}

	public static byte[] append(byte[] in, byte[] to_add)
	{
		byte[] r = new byte[in.length+to_add.length];
		
		System.arraycopy(in, 0, r, 0, in.length);
		System.arraycopy(to_add, 0, r, in.length, to_add.length);
		
		return r;
	}
	
	public static byte[] asn1_add(byte[] in, Asn1Protocol t)
	{
		return asn1_add(in, t.tag, t.value);
	}
	
	public static byte[] asn1_add(byte[] in, byte tag, Object obj)
	{
		if(obj instanceof byte[])
		{
			in = asn1_add(in, tag, (byte[])obj);
		}
		else if(obj instanceof Asn1Protocol[])
		{
			byte[] temp = new byte[0];
			Asn1Protocol[] asn1array = (Asn1Protocol[])obj;
			for(int i = 0; i < asn1array.length; i++)
			{
				temp = asn1_add(temp, asn1array[i].tag, asn1array[i].value);
			}
			in = asn1_add(in, tag, temp);
		}
		else
		{
			Asn1Protocol t = (Asn1Protocol)obj;
			in = asn1_add(in, t.tag, t.value);
		}
		
		return in;
	}
}
