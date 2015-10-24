README:

Overview:
Created four types of Indexes:
1) Raw Index:No Stemming or Stop words removal
2) Index without stopwords: Removed Stop Words
3) Index with Stemmed words only: Stemming done 
4) Index with both Stemming and Stop Words Removed: Both Stemming done and stop words processed.
---------------------------------------------------------------------------------------------------------------
Ran below mentioned models on the above indexes , obtained decent precision for all the models as compared to the elastic Search assignment.
----------------------------------------------------------------------------------------------------------------
For Proximity Search, I used the formula: (C - rangeOfWindow) * numOfContainTerms / (lengthOfDocument + V)  as mentioned in piazza
 - I ran proximity against the stemmed and stemmed&stop words removed indexes and got quite good precision values.
=======================================================================================================================

Description of the files in the Second_Assignment_IR folder.

1. Results Folder -> :
	-Contains the Summary Results text file which contains the statictics of the index, type and average precision for the Laplace Smoothing, BM25 and Okapi Models. Proximity Results have been obtained against two types of index: Index with Stemmed words and Index with Stemmed words and Stop words removed.
				1) The Results for top 100 docs have been mentioned for the above mentioned models & Indexes.
				2) The Results for the top 1000 docs have been mentioned for the above mentioned models & Indexes.
				3) The Results from last assignment obtained for top 100 docs have been mentioned.

	- Trec-Eval Results : This folder contains the folders:
		1) 100: Trec-Eval Results for all models
		2) 1000 : Trec-Eval Results for all models
		3) Top 100-100 Results: Query Wise output of top 100 or 1000 documents for all models.


2. Project source code: 
	Folder Name: IRAssignment2.
	Code for building index: 
		1) Raw Index: MergeHash4.java
		2) Index without stopwords: MergeHash5.java
		3) Index with Stemmed words only: MergeHash6.java
		4) Index with both Stemming and Stop Words Removed: MergeHash7.java

	Code for Models for type 1) and type 2) index:
	Code for OkapiTF : RunOkapi.java
	Code for BM25 : RunBM25.java
	Code for Unigram Laplace Smoothing: LaplaceSmoothing.java

	Code for Models for type 3) and type 4) index:
	Code for OkapiTF : RunOkapiStemmed.java
	Code for BM25 : RunBM25Stemmed.java
	Code for Unigram Laplace Smoothing: LaplaceSmoothingStemmed.java
	Code for Proximity Search: Proximity5.java
