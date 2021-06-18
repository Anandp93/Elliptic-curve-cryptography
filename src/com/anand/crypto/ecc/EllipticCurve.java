package com.anand.crypto.ecc;

import java.math.BigInteger;
import java.util.ArrayList;

import com.anand.crypto.ecc.Point;

public class EllipticCurve {

	static final BigInteger ZERO = BigInteger.ZERO, ONE = BigInteger.ONE, 
            TWO = new BigInteger("2"), THREE = new BigInteger("3"), 
            FOUR = new BigInteger("4"), TWENTY_SEVEN = new BigInteger("27"), 
            NEGATIVE_SIXTEEN = new BigInteger("-16");
	static final String SIZE = "%03d";
	private BigInteger a, b, p, n, orderE, h;
	private Point G = null;
	public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
		this.a = a; this.b = b; this.p = p;
		ArrayList<ArrayList<Point>> allPoints = findAllPointsOnCurve();
		this.orderE = countNumbOfPointsOnCurve(allPoints);
		displayAllPointsOnCurve(allPoints, this.orderE);
	}
	public boolean update(Point G) {
		String l = new String("+------------------------------------------------------------------------+");
		boolean flag = false;
		if (isPointOnCurve(G)) {
			flag = true;
			this.G = G;
			this.n = this.countNumbOfPointsInCyclicSubgroup(G);
			this.h = this.orderE.divide(this.n);
			System.out.println(l);
			System.out.println("| orderE (# points on curve) | n (subgroup size) | cofactor h (orderE/n) |");
			System.out.println(l);
			System.out.println("|           "+String.format(SIZE,orderE)+"            |         "+
			       String.format(SIZE,n) +"     |        "+ String.format(SIZE,h) +"          |");
			System.out.println(l);
		}
		return flag;
	}
	public Point slowMultiply(BigInteger d) { //only use w/ small cyclic subgroups
		ArrayList<StringBuffer> displaySB = new ArrayList<StringBuffer>();
		for(int i = 0; i< n.intValue(); i++) displaySB.add(new StringBuffer());
		BigInteger i = ONE;
		Point T = Point.POINT_AT_INFINITY;
		while (i.compareTo(d) <= 0) {
			if (!isInverse(G,T)) {
				if (G.equals(T)) T = duoble(G);
				else T = add(G, T);
			} else T = Point.POINT_AT_INFINITY;
			if (i.compareTo(n) > 0)
				displaySB.get(i.subtract(ONE).mod(n).intValue())
									.append("    "+String.format(SIZE,i)+"G="+T+" ");
			else displaySB.get(i.subtract(ONE).mod(n).intValue())
									.append(String.format(SIZE,i)+"G="+T+" ");
			i = i.add(ONE);
		}
		for(int j = 0; j< displaySB.size(); j++) 
			if(!displaySB.get(j).toString().equals("")) System.out.println(displaySB.get(j));
		displayResult(d, G, T);
		return T;
	}
	public Point fastMultiply(BigInteger d) {
		Point T = Point.POINT_AT_INFINITY;
		if (d.compareTo(n)<0) {
			StringBuffer sB = new StringBuffer();
			sB.append(d + "|Base10 = " + d.toString(2) + "|Base2\n"); 
			sB.append("===============================================\n");
			sB.append("step#  bit       double        & add (bit=1)\n");
			sB.append("===============================================\n");
			sB.append("[0]\t"+"1\t"+"[0G+0G=0G]\t" + "[0G+1G=1G]\t\n");
			sB.append("-----------------------------------------------\n");
			T = new Point(G.getX(), G.getY());
			BigInteger q =  ONE, p =  ONE;
			String dInBinary = d.toString(2);
			for (int i = 1; i < dInBinary.length(); i++) {
				int bit = Integer.parseInt(dInBinary.substring(i, i + 1));
				sB.append(" "+i + "\t"+bit + "\t "+q + "G+" + q + "G=");
				q = q.add(q);
				sB.append(q + "G\t");
				T = duoble(T);
				if (bit == 1) {
					sB.append(" "+q + "G+" + p + "G=");
					q = q.add(p);
					sB.append(q + "G\t");
					T = add(G,T);
				}
				if(i!=dInBinary.length()-1) sB.append("\n");
			}
			System.out.println(sB);
		}
		displayResult(d, G, T);
		return T;
	}
	private Point add(Point G, Point Q) {
		Point returnValue = null;
		if (G.equals(Q)) returnValue = duoble(G);
		else if(G.equals(Point.POINT_AT_INFINITY)) returnValue = Q;
		else if(Q.equals(Point.POINT_AT_INFINITY)) returnValue = G;
		else {
			BigInteger s = Q.getY().subtract(G.getY())
					.mod(p).multiply((Q.getX().subtract(G.getX())).modInverse(p));
			BigInteger Rx = s.multiply(s).subtract(G.getX()).subtract(Q.getX()).mod(p);
			BigInteger Ry = (s.multiply(G.getX().subtract(Rx))).subtract(G.getY()).mod(p);
			returnValue = new Point(Rx, Ry);
		}
		return returnValue;
	}
	private Point duoble(Point G) {
		Point returnValue = null;
		if(G.equals(Point.POINT_AT_INFINITY)) returnValue = G;
		else {
			BigInteger s = (THREE.multiply(G.getX().modPow(TWO,p)).add(a)).mod(p)
					.multiply(TWO.multiply(G.getY()).modInverse(p));
			BigInteger Rx = s.multiply(s).subtract(G.getX()).subtract(G.getX()).mod(p);
			BigInteger Ry = (s.multiply(G.getX().subtract(Rx))).subtract(G.getY()).mod(p);
			returnValue = new Point(Rx, Ry);
		}
		return returnValue;
	}
	static public boolean isNonsingular(BigInteger a, BigInteger b) {
		return !NEGATIVE_SIXTEEN.multiply(
				(FOUR.multiply(a.multiply(a).multiply(a)).add((
						TWENTY_SEVEN.multiply(b.multiply(b)))))).equals(ZERO);
	}
	private BigInteger countNumbOfPointsInCyclicSubgroup(Point G) {
		BigInteger n = ONE;
		Point T = Point.POINT_AT_INFINITY;
		while (n.compareTo(orderE) <= 0) {
			if (!isInverse(G,T)) {
				if (G.equals(T)) T = duoble(G);
				else T = add(G, T);
			} else break;
			n = n.add(ONE);
		}
		return n;
	}
	private boolean isInverse(Point G, Point T){
		return (p.compareTo(T.getY().add(G.getY()))== 0 && G.getX().compareTo(T.getX()) == 0);
	}
	private boolean isPointOnCurve(Point G){
		return G.getY().multiply(G.getY()).mod(p).equals(
			(G.getX().multiply(G.getX()).multiply(G.getX())).add((a.multiply(G.getX())).add(b)).mod(p));
	}
	private ArrayList<ArrayList<Point>> findAllPointsOnCurve() { //only use w/ small # of points
		ArrayList<ArrayList<Point>> allPoints = new ArrayList<ArrayList<Point>>();
		ArrayList<BigInteger> leftSide = new ArrayList<BigInteger>(), 
				              rightSide = new ArrayList<BigInteger>();
		BigInteger i = ZERO;
		while (i.compareTo(p) < 0) {
			rightSide.add((i.multiply(i).multiply(i)).add((a.multiply(i)).add(b)).mod(p));
			leftSide.add(i.multiply(i).mod(p));
			i = i.add(ONE);
		}
		i = ZERO;
		while (i.compareTo(p)<0) {
			BigInteger j = ZERO;
			ArrayList<Point> points = new ArrayList<Point>();
			while (j.compareTo(p) < 0) {	
				if(rightSide.get(i.intValue()).equals(leftSide.get(j.intValue())))
					points.add(new Point(i, j));
				j = j.add(ONE);
			}
			allPoints.add(points);
			i = i.add(ONE);
		}
		ArrayList<Point> pointAtInfinity = new ArrayList<Point>();
		pointAtInfinity.add(Point.POINT_AT_INFINITY);
		allPoints.add(pointAtInfinity);
		displayPointsMatchingTable(leftSide, rightSide);
		return allPoints;
	}
	private BigInteger countNumbOfPointsOnCurve(ArrayList<ArrayList<Point>> allPoints) {
		int numbOfPoints = 0;
		for (int i = 0; i <allPoints.size(); i++) {
			ArrayList<Point> points = allPoints.get(i);
			for (int j = 0; j <points.size(); j++) numbOfPoints++;
		}
		return BigInteger.valueOf(numbOfPoints);
    }
	private void displayPointsMatchingTable(ArrayList<BigInteger> leftSide, 
			                                ArrayList<BigInteger> rightSide){
		StringBuffer sB = new StringBuffer(), sB1 = new StringBuffer(), sB2 = new StringBuffer();
		sB.append("----------------------------------------------------------------\n");
		sB1.append("y^2 (mod "+p+") <congruent> x^3 + " +a+"x + "+b +" (mod "+ p+")");
		sB2.setLength(49-sB1.length());
		sB.append("  y   | "+sB1+sB2+"|  x\n");
		sB.append("----------------------------------------------------------------\n");
		for (int x=0; x<p.intValue(); x++) {
			sB.append(String.format(SIZE,x)+" | "+ String.format(SIZE,leftSide.get(x)) +"                                     "+ 
		         String.format(SIZE,rightSide.get(x)) +"  | "+ String.format(SIZE,x) +"\n");
		}
		sB.append("\n");
		System.out.print(sB);
	}
	private void displayAllPointsOnCurve(ArrayList<ArrayList<Point>> allPoints, BigInteger ordE){
		StringBuffer sB = new StringBuffer();
		sB.append("Found "+ordE+" Points satisfying "+this+"\n");
		for (int i=0; i<allPoints.size(); i++) {
			ArrayList<Point> points = allPoints.get(i);
			for (int j=0; j<points.size(); j++) sB.append(points.get(j));
			if (!points.isEmpty())sB.append("\n");
		}
		System.out.print(sB);
	}
	private void displayResult(BigInteger d, Point G, Point T) {
		System.out.println("+-------------------------------------+");
		System.out.println("| dG="+String.format(SIZE,d)+G +"="+T+" |");
		System.out.println("+-------------------------------------+");
	}
	public BigInteger getN() { return n; }
	public String toString() {return "E: y^2 <congruent> x^3 + "+a+"x +"+b + " mod "+p;}
}
