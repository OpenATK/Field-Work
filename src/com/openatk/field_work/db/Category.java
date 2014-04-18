package com.openatk.field_work.db;
import java.util.ArrayList;
import java.util.List;

import com.openatk.field_work.models.Job;

public class Category {
	private String title = null;
	private List<Job> jobs = null;
	private Integer acres = 0;
	
	public Category(){
		jobs = new ArrayList<Job>();
	}
	
	public Category(String title){
		this.title = title;
		jobs = new ArrayList<Job>();
	}
	
	public Category(String title, List<Job> jobs){
		this.title = title;
		this.jobs = jobs;
	}

	public void addJob(Job job) {
		jobs.add(job);
	}
	
	public void clearJobs() {
		jobs.clear();
	}
	
	public void removeJob(Job job) {
		//TODO
	}

	public void updateJob(Job job) {
		//TODO
	}
	
	public Job getJob(int pos){
		return jobs.get(pos);
	}
	
	public Integer countJobs(){
		return jobs.size();
	}
	
	public List<Job> getJobs(){
		return jobs;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void addAcres(Integer acres){
		this.acres = this.acres + acres;
	}
	
	public void setAcres(Integer acres){
		this.acres = acres;
	}
	
	public Integer getAcres(){
		return acres;
	}
}
