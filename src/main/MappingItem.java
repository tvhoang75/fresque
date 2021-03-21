/**
 * 
 */
package main;

import java.math.BigInteger;

import data.Data;

/**
 * @author vtran
 *
 */
public class MappingItem extends Data{
	private static final long serialVersionUID = 1L;
	private BigInteger code;
	
	public MappingItem() {
		code = new BigInteger("0");
	}
	
	public void accumulateCode(BigInteger newCode) {
		this.code = this.code.add(newCode);
	}
	
	public MappingItem(float indexedAttribute, BigInteger code) {
		this.indexedAttribute = indexedAttribute;
		this.code = code;
	}
	
	public MappingItem(float indexedAttribute) {
		this.indexedAttribute = indexedAttribute;
	}
	
	public BigInteger getCode() {
		return code;
	}

	public void setCode(BigInteger code) {
		this.code = code;
	}
}
