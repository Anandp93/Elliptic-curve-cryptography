package com.anand.crypto.ecc;

import java.math.BigInteger;

import com.anand.crypto.ecc.EllipticCurve;
import com.anand.crypto.ecc.Point;

public class Point {

	static BigInteger INFINITY = new BigInteger("99999999999999999999999999999999999999999999999999999999");
	static Point POINT_AT_INFINITY = new Point(EllipticCurve.ZERO, INFINITY);
	private BigInteger x;
	private BigInteger y;
	public Point(BigInteger x, BigInteger y) { this.x = x; this.y = y; }
	public BigInteger getX() { return x; }
	public BigInteger getY() { return y; }
	public boolean equals(Object object) {
		Point point = (Point)object;
		boolean flag = false;
		if (this.getY().equals(point.getY()) && this.getY().equals(INFINITY)) flag = true;
		else if (this.getX().equals(point.getX()) && this.getY().equals(point.getY()) ) flag = true;
		return flag;
	}
	public String toString() {
		String returnValue = "(" + String.format(EllipticCurve.SIZE,x) +
				","+String.format(EllipticCurve.SIZE,y)+")"; 
		if (y.equals(INFINITY)) returnValue = " O           ";
		return returnValue;
	}
}

