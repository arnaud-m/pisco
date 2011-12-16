/*********************************************
 * OPL 12.3 Model
 * Author: Arnaud Malapert
 * Creation Date: 16 déc. 2011 at 15:27:14
 *********************************************/

 include "batch-processing-common.mod";

 
 dvar int+ JobCompletionTimes[1..NbJobs];
 
 subject to {
 
  forall( i in Jobs )
    forall( k in Batches ) 	
  	ctJobCtimes:
  	SumDurations * ( 1 - Takes[i][k]) + JobCompletionTimes[i] >= CompletionTimes[k]; 
   
  
  ctWFlow:
    sum(i in Jobs) JobCompletionTimes[i] == obj; 
 }  