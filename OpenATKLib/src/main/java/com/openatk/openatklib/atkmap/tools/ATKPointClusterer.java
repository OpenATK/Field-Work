package com.openatk.openatklib.atkmap.tools;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.models.ATKPointCluster;
import com.openatk.openatklib.atkmap.views.ATKPointView;

public class ATKPointClusterer {
	
	
	private List<ATKPoint> points;
	private List<ATKPointView> atkPointViews;
	private List<ATKPointCluster> clusters = new ArrayList<ATKPointCluster>();
	
	private double clusterSize = 10.0f;
	private LatLng position;
	private GoogleMap map;
	
	public ATKPointClusterer(GoogleMap map){
		this.map = map;
	}
	
	public void add(ATKPoint point){
		points.add(point);
		ATKPointView pointView = new ATKPointView(map,point);
		pointView.disableDrawing(true);
		
		//Does this belong in a existing cluster
		double minDist = 0.0f;
		ATKPointCluster closestCluster = null;
		for(int i=0; i<clusters.size(); i++){
			ATKPointCluster cluster = clusters.get(i);
			double dist = distanceBetween(cluster.position, point.position);
			if(minDist > dist || closestCluster == null){
				minDist = dist;
				closestCluster = cluster;
			}
		}
		
		if(closestCluster != null && minDist <= this.clusterSize){
			closestCluster.add(point);
		}
	}
	
	public void remove(ATKPoint point){
		points.remove(point);
	}
	
	private void calcCentroid(){
		
	}
	
	private double distanceBetween(LatLng a, LatLng b){
		Projection proj = this.map.getProjection();
		Point screenPosA = proj.toScreenLocation(a);
		Point screenPosB = proj.toScreenLocation(b);
		
		//TODO Calculate distance between
		
		return 0;
	}
}
