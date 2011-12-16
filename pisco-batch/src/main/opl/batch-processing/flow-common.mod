/*********************************************
 * OPL 12.3 Model
 * Author: nono
 * Creation Date: 16 déc. 2011 at 21:38:21
 *********************************************/

include "batch-processing-common.mod";

 
 dvar int+ JobCompletionTimes[1..NbJobs];
 
 subject to {
 
  forall( i in Jobs )
  	forall( k in Batches ) 	
  	ctJobCtimes:
  	SumDurations * ( 1 - Takes[i][k]) + JobCompletionTimes[i] >= CompletionTimes[k]; 
   
 }
 
  execute DISPLAY2 {
 
  writeln("\nJobCTime=", JobCompletionTimes);

};  