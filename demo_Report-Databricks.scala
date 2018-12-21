// Databricks notebook source
// MAGIC %md # **Field Eng Coding Challenges**
// MAGIC 
// MAGIC ## Instructions
// MAGIC 
// MAGIC  1. Clone this notebook to your home folder
// MAGIC  1. Unless otherwise instructed, solve as many of the problems below as you can within the alloted time frame. Some of the challenges are more advanced than others and are expected to take more time to solve.
// MAGIC  1. You can create as many notebooks as you would like to answer the challenges 
// MAGIC  1. Notebooks should be presentable and should be able to execute succesfully with `Run All`
// MAGIC  1. Notebooks should be idempotent as well. Ideally, you'll also clean up after yourself (i.e. drop your tables)
// MAGIC  1. Once completed, publish your notebook: 
// MAGIC    * Choose the `Publish` item on the `File` menu
// MAGIC  1. Copy the URL(s) of the published notebooks and email them back to your Databricks contacts

// COMMAND ----------

// MAGIC %md 
// MAGIC # Welcome, Evaluator
// MAGIC 
// MAGIC Thank you for evaluating Jason Beach's application notebook.  I tried to provide solutions to questions clearly and succinctly.  Multiple methods are used when appropriate to demonstrate different options.  Python is used throughout this notebook because that is the preferred language for the Team to which I am applying.
// MAGIC Please make note of: 
// MAGIC ```python
// MAGIC #comments within code
// MAGIC ```
// MAGIC They provide more thorough explanations.
// MAGIC Thanks for your time, and enjoy the show!

// COMMAND ----------

sc

// COMMAND ----------

sqlContext

// COMMAND ----------

// MAGIC %md ## Challenges
// MAGIC ---
// MAGIC 
// MAGIC ### TPC-H Dataset
// MAGIC You're provided with a TPCH data set. The data is located in `/databricks-datasets/tpch/data-001/`. You can see the directory structure below:

// COMMAND ----------

// MAGIC %fs head /databricks-datasets/tpch/data-001/README.md 

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/tpch/data-001/"))

// COMMAND ----------

// MAGIC %md As you can see above, this dataset consists of 8 different folders with different datasets. The schema of each dataset is demonstrated below: 

// COMMAND ----------

// MAGIC %md ![test](http://kejser.org/wp-content/uploads/2014/06/image_thumb2.png)

// COMMAND ----------

// MAGIC %md You can take a quick look at each dataset by running the following Spark commmand. Feel free to explore and get familiar with this dataset

// COMMAND ----------

sc.textFile("/databricks-datasets/tpch/data-001/supplier/").take(1)

// COMMAND ----------

// MAGIC %md #### **Question #1**: Joins in Core Spark
// MAGIC Pick any two datasets and join them using Spark's API. Feel free to pick any two datasets. For example: `PART` and `PARTSUPP`. The goal of this exercise is not to derive anything meaningful out of this data but to demonstrate how to use Spark to join two datasets. For this problem you're **NOT allowed to use SparkSQL**. You can only use RDD API. You can use either Python or Scala to solve this problem.

// COMMAND ----------

// MAGIC %python
// MAGIC # joining nations to regions
// MAGIC nat = sc.textFile("/databricks-datasets/tpch/data-001/nation/")
// MAGIC reg = sc.textFile("/databricks-datasets/tpch/data-001/region/")
// MAGIC nat_ed = nat.map(lambda x: x.split('|')).map(lambda x: (x[2],x))
// MAGIC reg_ed = reg.map(lambda x: x.split('|')).map(lambda x: (x[0],x))
// MAGIC result = nat_ed.join(reg_ed)
// MAGIC result.take(1)

// COMMAND ----------

// MAGIC %python #clean-up
// MAGIC nat.unpersist()
// MAGIC reg.unpersist()
// MAGIC result.unpersist()

// COMMAND ----------

// MAGIC %md #### **Question #2**: Joins With Spark SQL
// MAGIC Pick any two datasets and join them using SparkSQL API. Feel free to pick any two datasets. For example: PART and PARTSUPP. The goal of this exercise is not to derive anything meaningful out of this data but to demonstrate how to use Spark to join two datasets. For this problem you're **NOT allowed to use the RDD API**. You can only use SparkSQL API. You can use either Python or Scala to solve this problem. 

// COMMAND ----------

// MAGIC %python
// MAGIC # more appropriate join of nations to regions using dataframes
// MAGIC nat = sc.textFile("/databricks-datasets/tpch/data-001/nation/").map(lambda x: x.split('|'))
// MAGIC reg = sc.textFile("/databricks-datasets/tpch/data-001/region/").map(lambda x: x.split('|'))
// MAGIC dfNation = nat.toDF(["NationKey","NationName","RegionKey","NationComment"]).drop("_5")
// MAGIC dfRegion = reg.toDF(["RegionKeyTmp","RegionName","RegionComment"]).drop("_4")
// MAGIC dfResult = dfNation.join(dfRegion, dfNation.RegionKey==dfRegion.RegionKeyTmp, "inner").drop("RegionKeyTmp").sort("RegionKey", ascending=True)
// MAGIC dfResult.show(5)

// COMMAND ----------

// MAGIC %python
// MAGIC # note: for reading-in data, this method works too, and may be preferred, these days
// MAGIC dfNation = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").option("inferSchema", "false").option("delimiter", "|").load("/databricks-datasets/tpch/data-001/nation/")

// COMMAND ----------

// MAGIC %python #clean-up
// MAGIC dfNation.unpersist()
// MAGIC dfRegion.unpersist()
// MAGIC # dfResult.unpersist() not yet!

// COMMAND ----------

// MAGIC %md #### **Question #3**: Alternate Data Formats
// MAGIC The given dataset above is in raw text storage format. What other data storage format can you suggest to optimize the performance of our Spark workload if we were to frequently scan and read this dataset. Please come up with a code example and explain why you decide to go with this approach. Please note that there's no completely correct answer here. We're interested to hear your thoughts and see the implementation details.shell/1282

// COMMAND ----------

// MAGIC %python
// MAGIC # I would save to apache parquet format because it provides for efficient compression and encoding schemas.
// MAGIC # This reduction in storage space leads to an increase in cost savings.
// MAGIC # Additional benefits come from using parquet within amazon aws.  An example is that making use of the parquet 
// MAGIC # columnar-format, queries from Redshift do not have to scan as much data.  Again, this leads to lower costs.
// MAGIC #
// MAGIC # Below, we see this hypothesis in-action on the current dataframe.  We reduce data to one partition in order to save the data to a single file.  The files sizes are:
// MAGIC #   * .parquet - 3867 bytes
// MAGIC #   * .csv - 4081 bytes
// MAGIC #
// MAGIC # parquet
// MAGIC dfResult.coalesce(1).write.mode('overwrite').save("/tmp/Result_parquet",format="parquet")
// MAGIC dfNewResultParq = spark.read.load("Result.parquet")
// MAGIC # csv
// MAGIC dfResult.coalesce(1).write.mode('overwrite').csv("/tmp/Result_csv")
// MAGIC dfNewResultCsv = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/tmp/Result_csv")

// COMMAND ----------

// MAGIC %fs ls dbfs:/tmp/Result_parquet/

// COMMAND ----------

// MAGIC %fs ls dbfs:/tmp/Result_csv/

// COMMAND ----------

// MAGIC %python #clean-up
// MAGIC dfResult.unpersist()
// MAGIC dfNewResultParq.unpersist()
// MAGIC dfNewResultCsv.unpersist()

// COMMAND ----------

// MAGIC %md ### Baby Names Dataset
// MAGIC 
// MAGIC This dataset comes from a website referenced by [Data.gov](http://catalog.data.gov/dataset/baby-names-beginning-2007). It lists baby names used in the state of NY from 2007 to 2012.
// MAGIC 
// MAGIC The following cells run commands that copy this file to the cluster.

// COMMAND ----------

// MAGIC %fs rm dbfs:/tmp/rows.json

// COMMAND ----------

import java.net.URL
import java.io.File
import org.apache.commons.io.FileUtils

val tmpFile = new File("/tmp/rows.json")
FileUtils.copyURLToFile(new URL("https://health.data.ny.gov/api/views/jxy9-yhdk/rows.json?accessType=DOWNLOAD"), tmpFile)

// COMMAND ----------

// MAGIC %fs mv file:/tmp/rows.json dbfs:/tmp/rows.json

// COMMAND ----------

// MAGIC %fs head dbfs:/tmp/rows.json

// COMMAND ----------

// MAGIC %md #### **Question #1**: Spark SQL's Native JSON Support
// MAGIC Use Spark SQL's native JSON support to create a temp table you can use to query the data (you'll use the `registerTempTable` operation). Show a simple sample query.

// COMMAND ----------

// MAGIC %python
// MAGIC # Basic multi-line read of nested json file
// MAGIC nested = sqlContext.read.option("multiline", "true").json("/tmp/rows.json")
// MAGIC print(nested.count())
// MAGIC nested.show()

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.functions import explode, monotonically_increasing_id
// MAGIC 
// MAGIC nestid = nested.withColumn("id", monotonically_increasing_id() )
// MAGIC flat = nestid.select("id", explode("data").alias("col") )
// MAGIC dfData = flat.select("id", flat.col[0], flat.col[1], flat.col[2], flat.col[3], flat.col[4], flat.col[5], flat.col[6], flat.col[7], flat.col[8], flat.col[9], flat.col[10], flat.col[11], flat.col[12])
// MAGIC print(dfData.count())
// MAGIC dfData.show(5)
// MAGIC # This data isn't too useful without the column names.  In Question 2, let's get the meta-data to fix this dataframe.

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # This basic query for number of records matches with dataframe count 
// MAGIC dfData.registerTempTable("data")
// MAGIC sqlContext.sql("SELECT Count(*) FROM data").show()

// COMMAND ----------

// MAGIC %md #### **Question #2**: Working with Nested Data
// MAGIC What does the nested schema of this dataset look like? How can you bring these nested fields up to the top level in a DataFrame?

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.functions import explode, monotonically_increasing_id
// MAGIC 
// MAGIC # We can flatten nested structures depending on their type: struct or list.  
// MAGIC # The first method is '*-expansion' and is used on a struct.
// MAGIC meta_1 = nestid.select("id", "meta.*" )
// MAGIC meta_2 = meta_1.select("id", "view.*")
// MAGIC dfMeta = meta_2
// MAGIC dfMeta.printSchema()
// MAGIC # Further flatenning can be continued in the same dataframe; however, the number of columns makes it unwieldly, as well as difficult to see how columns are grouped.
// MAGIC # A good option may be to extract nested data into individual dataframes.  A useful example is 'column information'.  After we flatten, we can provide this data to the Question 1 dataframe so we can make use of it.

// COMMAND ----------

// MAGIC %python 
// MAGIC from pyspark.sql.functions import monotonically_increasing_id 
// MAGIC 
// MAGIC # The second method of expanding column values uses the 'explode()' function and is used with lists.
// MAGIC dfColumns = dfMeta.select("columns").withColumn( "colElem", explode("columns"))
// MAGIC dfColNames = dfColumns.select("colElem.*").withColumn("id", monotonically_increasing_id()).select("id","name")
// MAGIC dfColNames.show()

// COMMAND ----------

// MAGIC %python
// MAGIC import functools as fun
// MAGIC 
// MAGIC data = dfData
// MAGIC oldColumns = data.schema.names
// MAGIC newColumns = ["index"] + [nm['name'].replace(" ","_") for nm in dfColNames.select("name").collect()]
// MAGIC df = fun.reduce(lambda data, idx: data.withColumnRenamed(oldColumns[idx], newColumns[idx]), range(len(oldColumns)), data)
// MAGIC 
// MAGIC # With the addition of the column names (from the meta-data), our dataframe is now complete.
// MAGIC dfDataClean = df.select('position','Year','First_Name', 'County', 'Sex', 'Count')
// MAGIC dfDataClean.show(3)

// COMMAND ----------

// MAGIC %md #### **Question #3**: Executing Full Data Pipelines
// MAGIC Create a second version of the answer to Question 2, and make sure one of your queries makes the original web call every time a query is run, while another version only executes the web call one time.

// COMMAND ----------

// MAGIC %python
// MAGIC # I assume this question means printSchema() when referring to 'queries', in Question 2.  I did get a response when I asked (by email) for clarification on this question, but I probably should have asked more questions on it.  I am assuming the phrase 'full data pipelines' refers to the specific meaning of data transformation stages in the spark api context. 
// MAGIC # Below is my best guess as to what the evaluator wants.
// MAGIC # I assume that this question requires the following: 
// MAGIC #  i) spark datapipeline
// MAGIC #  ii) transformation to get file if it doesn't exist
// MAGIC #  iii) transformation to get file and overwrite if it exists
// MAGIC #  iv) printSchema()

// COMMAND ----------

// MAGIC %python # test http requests is working
// MAGIC import requests
// MAGIC 
// MAGIC URL = "https://health.data.ny.gov/api/views/jxy9-yhdk/rows.json?accessType=DOWNLOAD"
// MAGIC DST = "/dbfs/tmp/rows_test.json"
// MAGIC r = requests.get(URL, allow_redirects=True)
// MAGIC open(DST, 'wb').write(r.content)

// COMMAND ----------

// MAGIC %python
// MAGIC import pyspark.sql.functions as F
// MAGIC from pyspark.ml import Pipeline, Transformer
// MAGIC from pyspark.ml.feature import Bucketizer
// MAGIC from pyspark.sql import DataFrame
// MAGIC from typing import Iterable
// MAGIC import pandas as pd
// MAGIC import requests
// MAGIC 
// MAGIC # CUSTOM TRANSFORMERS ----------------------------------------------------------------
// MAGIC class CheckDataEachTime(Transformer):
// MAGIC     """
// MAGIC     Custom Transformer to check updated data
// MAGIC     """
// MAGIC     URL = "https://health.data.ny.gov/api/views/jxy9-yhdk/rows.json?accessType=DOWNLOAD"
// MAGIC     DST = "/dbfs/tmp/rows_test.json"
// MAGIC 
// MAGIC     def __init__(self):
// MAGIC       super(CheckDataEachTime, self).__init__()
// MAGIC 
// MAGIC     def _transform(self, df: DataFrame) -> DataFrame:
// MAGIC       r = requests.get(self.URL, allow_redirects=True)
// MAGIC       open(self.DST, 'wb').write(r.content)
// MAGIC       nested = sqlContext.read.option("multiline", "true").json("/tmp/rows.json")
// MAGIC       nestid = nested.withColumn("id", monotonically_increasing_id() )
// MAGIC       meta_1 = nestid.select("id", "meta.*" )
// MAGIC       meta_2 = meta_1.select("id", "view.*")
// MAGIC       new_cnt = meta_2.count()
// MAGIC       old_cnt = df.count()
// MAGIC       if new_cnt != old_cnt:
// MAGIC         dfReturned = nestid
// MAGIC       else:
// MAGIC         dfReturned = df
// MAGIC       return dfReturned

// COMMAND ----------

// MAGIC %python
// MAGIC import os
// MAGIC 
// MAGIC class CheckDataOnce(Transformer):
// MAGIC     """
// MAGIC     Custom Transformer to check data was downloaded, at least once
// MAGIC     """
// MAGIC     URL = "https://health.data.ny.gov/api/views/jxy9-yhdk/rows.json?accessType=DOWNLOAD"
// MAGIC     DST = "/dbfs/tmp/rows_test.json"
// MAGIC 
// MAGIC     def __init__(self):
// MAGIC       super(CheckDataOnce, self).__init__()
// MAGIC 
// MAGIC     def _transform(self, df: DataFrame) -> DataFrame:
// MAGIC       exists = os.path.isfile(DST)
// MAGIC       if exists:
// MAGIC         dfReturned = df
// MAGIC         return dfReturned
// MAGIC       else:
// MAGIC         r = requests.get(self.URL, allow_redirects=True)
// MAGIC         open(self.DST, 'wb').write(r.content)
// MAGIC         nested = sqlContext.read.option("multiline", "true").json("/tmp/rows.json")
// MAGIC         nestid = nested.withColumn("id", monotonically_increasing_id() )
// MAGIC         meta_1 = nestid.select("id", "meta.*" )
// MAGIC         meta_2 = meta_1.select("id", "view.*")
// MAGIC         new_cnt = meta_2.count()
// MAGIC         old_cnt = df.count()
// MAGIC         if new_cnt != old_cnt:
// MAGIC           dfReturned = nestid
// MAGIC         else:
// MAGIC           dfReturned = df
// MAGIC         return dfReturned

// COMMAND ----------

// MAGIC %python
// MAGIC class PrintSchema(Transformer):
// MAGIC     """
// MAGIC     Custom Transformer to print the schema
// MAGIC     """
// MAGIC     
// MAGIC     def __init__(self):
// MAGIC       super(PrintSchema, self).__init__()
// MAGIC       
// MAGIC     def _transform(self, df: DataFrame) -> DataFrame:
// MAGIC       df.printSchema()
// MAGIC       return df
// MAGIC     

// COMMAND ----------

// MAGIC %python
// MAGIC # After running the pipeline, visually inspect file exists
// MAGIC df = dfMeta
// MAGIC check_data_once = CheckDataOnce()
// MAGIC print_schema = PrintSchema()
// MAGIC Pipeline(stages=[check_data_once, print_schema]).fit(df)

// COMMAND ----------

// MAGIC %python
// MAGIC # After running the pipeline, visually inspect file exists
// MAGIC df = dfMeta
// MAGIC check_data_each_time = CheckDataEachTime()
// MAGIC print_schema = PrintSchema()
// MAGIC Pipeline(stages=[check_data_each_time, print_schema]).fit(df)

// COMMAND ----------

// MAGIC %fs ls /tmp

// COMMAND ----------

// MAGIC %md #### **Question #4**: Analyzing the Data
// MAGIC 
// MAGIC Using the tables you created, create a simple visualization that shows what is the most popular first letters baby names to start with in each year.

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql import functions as fun
// MAGIC 
// MAGIC # Prepare the data for ranking
// MAGIC dfDataClean.registerTempTable("names")
// MAGIC dfSub_1 = sqlContext.sql("SELECT Year, Substr(First_Name,0,1) as Letter, Sum(Count) as SubTotal \
// MAGIC                             FROM names \
// MAGIC                             GROUP BY Year, Letter \
// MAGIC                             ORDER BY Year Desc, SubTotal Desc")
// MAGIC dfSub_1 = dfSub_1.withColumn("id", fun.monotonically_increasing_id())
// MAGIC dfSub_1.registerTempTable("names_sub_1")
// MAGIC dfSub_1.show(3)
// MAGIC 
// MAGIC # Take the top 3 of each year
// MAGIC dfSub_2 = sqlContext.sql("SELECT a.id, COUNT(*) AS ranknum \
// MAGIC                         FROM names_sub_1 AS a \
// MAGIC                         INNER JOIN names_sub_1 AS b \
// MAGIC                         ON (a.Year = b.Year) AND (a.SubTotal <= b.SubTotal) \
// MAGIC                         GROUP BY a.id \
// MAGIC                         HAVING COUNT(*) <= 3")
// MAGIC dfSub_2.registerTempTable("names_sub_2")
// MAGIC dfSub_2.show(5)
// MAGIC 
// MAGIC dfResult = sqlContext.sql("SELECT c.*, d.ranknum \
// MAGIC                           FROM names_sub_1 AS c \
// MAGIC                           INNER JOIN names_sub_2 AS d \
// MAGIC                             ON (c.id = d.id) \
// MAGIC                           ORDER BY c.Year desc, d.ranknum")
// MAGIC dfResult.show(6)

// COMMAND ----------

// MAGIC %python 
// MAGIC # If I had more time, I would use percent of year-total, rather than raw count
// MAGIC # I do not know the cause for the increase in counts that occurs at 2017.  This is probably an expansion in the data collection process - not an increase in overall babies birthed.  Only careful inspection of the data and discussions with stakeholders / data-providers can elucidate this.
// MAGIC from ggplot import *
// MAGIC dfData = dfResult.toPandas()
// MAGIC p = ggplot(dfData, aes(x='Year', y='SubTotal', colour='Letter', group='Letter')) + geom_line()
// MAGIC display(p)

// COMMAND ----------

// MAGIC %md ### Log Processing
// MAGIC 
// MAGIC The following data comes from the _Learning Spark_ book.

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/learning-spark/data-001/fake_logs"))

// COMMAND ----------

println(dbutils.fs.head("/databricks-datasets/learning-spark/data-001/fake_logs/log1.log"))

// COMMAND ----------

// MAGIC %md #### **Question #1**: Parsing Logs
// MAGIC Parse the logs in to a DataFrame/Spark SQL table that can be queried. This should be done using the Dataset API.

// COMMAND ----------

// MAGIC %python
// MAGIC logs = sc.textFile("/databricks-datasets/learning-spark/data-001/fake_logs/")
// MAGIC logs.count()

// COMMAND ----------

// MAGIC %python #METHOD-1: RegEx
// MAGIC from pyspark.sql import Row
// MAGIC import re
// MAGIC parts = [
// MAGIC     r'(?P<host>\S+)',                   # host 
// MAGIC     r'\S+',                             # indent (unused)
// MAGIC     r'(?P<user>\S+)',                   # user 
// MAGIC     r'\[(?P<time>.+)\]',                # time 
// MAGIC     r'"(?P<request>.*)"',               # request 
// MAGIC     r'(?P<status>[0-9]+)',              # status 
// MAGIC     r'(?P<size>\S+)',                   # size 
// MAGIC     r'"(?P<referrer>.*)"',              # referrer 
// MAGIC     r'"(?P<agent>.*)"',                 # user agent 
// MAGIC ]
// MAGIC pattern = re.compile(r'\s+'.join(parts)+r'\s*\Z')
// MAGIC 
// MAGIC prs = logs.map(lambda x: pattern.match(x).groupdict() )
// MAGIC rows = prs.map(lambda x: Row(**x))
// MAGIC dfLogs = rows.toDF()
// MAGIC dfLogs.show()

// COMMAND ----------

// MAGIC %python #METHOD-2: manual means
// MAGIC ip = logs.map(lambda x: x.split(" -")[0]).map(lambda x: x.strip()).zipWithIndex()
// MAGIC date = logs.map(lambda x: x.split("[")[1]).map(lambda x: x.split(" +")[0]).map(lambda x: x.replace(":"," ",1)).zipWithIndex()
// MAGIC verb = logs.map(lambda x: x.split("\"")[1]).map(lambda x: x.split("\"")[0]).zipWithIndex()
// MAGIC object = logs.map(lambda x: x.split("\"")[1]).map(lambda x: x.split(" ")[1]).zipWithIndex()
// MAGIC status = logs.map(lambda x: x.split("\"")[2]).map(lambda x: x.strip()).map(lambda x: x[:3]).zipWithIndex()
// MAGIC obj_size = logs.map(lambda x: x.split("\"")[2]).map(lambda x: x.strip()).map(lambda x: x.split(" ")[1]).zipWithIndex()
// MAGIC page = logs.map(lambda x: x.split("\"Mozilla/5.0")[1]).map(lambda x: x.replace("\"","")).zipWithIndex()
// MAGIC 
// MAGIC tmp = sc.union([ip, date, verb, object, status, obj_size, page]).map(lambda x: (x[1],x[0])).groupByKey().mapValues(list).map(lambda x: x[1])

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.functions import dayofmonth, from_unixtime, month, unix_timestamp, year, to_date, lit
// MAGIC 
// MAGIC dfTmp = tmp.toDF(["ip","date","verb","object","status","obj_size","page"])
// MAGIC ts = from_unixtime(unix_timestamp(dfTmp['date'], 'dd/MMM/yyyy HH:mm:ss'))
// MAGIC dfManual = dfTmp.withColumn("timestamp",ts)
// MAGIC dfManual.show()

// COMMAND ----------

// MAGIC %md #### **Question #2**: Analysis
// MAGIC Generate some insights from the log data.

// COMMAND ----------

// MAGIC %python
// MAGIC # There is not much data, here
// MAGIC dfLogs.groupBy("host").count().show()
// MAGIC dfLogs.groupBy("status").count().show()
// MAGIC 
// MAGIC # Try some timestamp filtering
// MAGIC from pyspark.sql.types import TimestampType
// MAGIC s = "2014-09-24"
// MAGIC checkdate = to_date(lit(s)).cast(TimestampType())
// MAGIC dfManual.where((dfManual.timestamp > checkdate)).show()

// COMMAND ----------

// MAGIC %python
// MAGIC dfManual.unpersist()
// MAGIC dfLogs.unpersist()

// COMMAND ----------

// MAGIC %md
// MAGIC ### CSV Parsing
// MAGIC The following examples involove working with simple CSV data

// COMMAND ----------

// MAGIC %md #### **Question #1**: CSV Header Rows
// MAGIC Given the simple RDD `full_csv` below, write the most efficient Spark job you can to remove the header row

// COMMAND ----------

// MAGIC %python
// MAGIC full_csv = sc.parallelize([
// MAGIC   "col_1, col_2, col_3",
// MAGIC   "1, ABC, Foo1",
// MAGIC   "2, ABCD, Foo2",
// MAGIC   "3, ABCDE, Foo3",
// MAGIC   "4, ABCDEF, Foo4",
// MAGIC   "5, DEF, Foo5",
// MAGIC   "6, DEFGHI, Foo6",
// MAGIC   "7, GHI, Foo7",
// MAGIC   "8, GHIJKL, Foo8",
// MAGIC   "9, JKLMNO, Foo9",
// MAGIC   "10, MNO, Foo10"])
// MAGIC   

// COMMAND ----------

// MAGIC %python
// MAGIC # Remove header
// MAGIC rddResult = full_csv.zipWithIndex().filter(lambda x: x[1]>0).map(lambda x: x[0])
// MAGIC rddResult.collect()

// COMMAND ----------

// MAGIC %md #### **Question #2**: SparkSQL Dataframes
// MAGIC Using the `full_csv` RDD above, write code that results in a DataFrame where the schema was created programmatically based on the head row. Create a second RDD similair to `full_csv` and uses the same function(s) you created in this step to make a Dataframe for it

// COMMAND ----------

// MAGIC %python
// MAGIC import pyspark.sql.types as typ
// MAGIC 
// MAGIC # Convert the header to a schema, programmatically
// MAGIC hdr = full_csv.zipWithIndex().filter(lambda x: x[1]==0).map(lambda x: x[0])
// MAGIC schemaElements = hdr.flatMap(lambda x: x.split(", ")).map(lambda x: typ.StructField(x, typ.StringType(), True)).collect()
// MAGIC schema = typ.StructType( schemaElements )
// MAGIC schema

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # Load data into dataframe with schema
// MAGIC rddSplit = rddResult.map(lambda x: x.split(", "))
// MAGIC dfResult_2 = rddSplit.toDF(schema)
// MAGIC dfResult_2.show()

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # By using different data, we can support that this is a reproduceable process, add it to a function, and use it for other data.  However, the weakness lies in that it cannot infer types; hence, it will only produce StringType() values.
// MAGIC new_csv = sc.parallelize([
// MAGIC   "col_1, col_2, col_3",
// MAGIC   "11, AB, Foo11",
// MAGIC   "12, ABC, Foo12",
// MAGIC   "13, ABCD, Foo13",
// MAGIC   "14, ABCD, Foo14",
// MAGIC   "15, DE, Foo15",
// MAGIC   "16, DEFG, Foo16",
// MAGIC   "17, GH, Foo17",
// MAGIC   "18, GHI, Foo18",
// MAGIC   "19, JKL, Foo19",
// MAGIC   "20, MNO, Foo20"])
// MAGIC   
// MAGIC import pyspark.sql.types as typ
// MAGIC 
// MAGIC hdr = new_csv.zipWithIndex().filter(lambda x: x[1]==0).map(lambda x: x[0])
// MAGIC schemaElements = hdr.flatMap(lambda x: x.split(", ")).map(lambda x: typ.StructField(x, typ.StringType(), True)).collect()
// MAGIC schema = typ.StructType( schemaElements )
// MAGIC 
// MAGIC new_rdd = new_csv.zipWithIndex().filter(lambda x: x[1]>0).map(lambda x: x[0])
// MAGIC rddSplit = new_rdd.map(lambda x: x.split(", "))
// MAGIC dfResult_3 = rddSplit.toDF(schema)
// MAGIC dfResult_3.show()

// COMMAND ----------

// MAGIC %md #### **Question #3**: Parsing Pairs
// MAGIC 
// MAGIC Write a Spark job that processes comma-seperated lines that look like the below example to pull out Key Value pairs. 
// MAGIC 
// MAGIC Given the following data:
// MAGIC 
// MAGIC ~~~
// MAGIC Row-Key-001, K1, 10, A2, 20, K3, 30, B4, 42, K5, 19, C20, 20
// MAGIC Row-Key-002, X1, 20, Y6, 10, Z15, 35, X16, 42
// MAGIC Row-Key-003, L4, 30, M10, 5, N12, 38, O14, 41, P13, 8
// MAGIC ~~~
// MAGIC 
// MAGIC You'll want to create an RDD that contains the following data:
// MAGIC 
// MAGIC ~~~
// MAGIC Row-Key-001, K1
// MAGIC Row-Key-001, A2
// MAGIC Row-Key-001, K3
// MAGIC Row-Key-001, B4
// MAGIC Row-Key-001, K5
// MAGIC Row-Key-001, C20
// MAGIC Row-Key-002, X1
// MAGIC Row-Key-002, Y6
// MAGIC Row-Key-002, Z15
// MAGIC Row-Key-002, X16
// MAGIC Row-Key-003, L4
// MAGIC Row-Key-003, M10
// MAGIC Row-Key-003, N12
// MAGIC Row-Key-003, O14
// MAGIC Row-Key-003, P13
// MAGIC ~~~

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.functions import explode, split
// MAGIC data = [["Row-Key-001", "K1", 10, "A2", 20, "K3", 30, "B4", 42, "K5", 19, "C20", 20], \
// MAGIC ["Row-Key-002", "X1", 20, "Y6", 10, "Z15", 35, "X16", 42], \
// MAGIC ["Row-Key-003", "L4", 30, "M10", 5, "N12", 38, "O14", 41, "P13", 8]]
// MAGIC 
// MAGIC # use rdd and dataframe api, then convert back to rdd
// MAGIC rdd = sc.parallelize( data ).map(lambda x: (x[0], ', '.join([v for i,v in enumerate(x) if (i!=0 and (type(v) is str)) ] )) )
// MAGIC df = rdd.toDF(["key","val"])
// MAGIC dfResult = df.withColumn("val", explode(split("val", ",")))
// MAGIC dfResult.rdd.map(list).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC #### Question #4 Create Tables Programmatically And Cache The Table
// MAGIC 
// MAGIC Create a table using Scala or Python
// MAGIC 
// MAGIC * Use `CREATE EXTERNAL TABLE` in SQL, or `DataFrame.saveAsTable()` in Scala or Python, to register tables.
// MAGIC * Please refer to the [Accessing Data](/#workspace/databricks_guide/03 Accessing Data/0 Accessing Data) guide for how to import specific data types.
// MAGIC 
// MAGIC ### Temporary Tables
// MAGIC * Within each Spark cluster, temporary tables registered in the `sqlContext` with `DataFrame.registerTempTable` will also be shared across the notebooks attached to that Databricks cluster.
// MAGIC   * Run `someDataFrame.registerTempTable(TEMP_TABLE_NAME)` to give register a table.
// MAGIC * These tables will not be visible in the left-hand menu, but can be accessed by name in SQL and DataFrames.

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.types import StructField, StructType, LongType, StringType
// MAGIC 
// MAGIC 
// MAGIC rddNew = sc.parallelize((
// MAGIC {"id": 123, "name": "Kat", "age": 24, "hair": "brown"  },\
// MAGIC {"id": 234, "name": "Mike", "age": 28, "hair": "black"  },\
// MAGIC {"id": 345, "name": "Sean", "age": 27, "hair": "blond" }))
// MAGIC 
// MAGIC mySchema = StructType([StructField("id", LongType(), True),\
// MAGIC                        StructField("name", StringType(),True),\
// MAGIC                        StructField("age", LongType(), True),\
// MAGIC                        StructField("hair", StringType(), True), ])
// MAGIC 
// MAGIC newDf = sqlContext.createDataFrame(rddNew, mySchema)
// MAGIC newDf.printSchema()
// MAGIC #newDf.saveAsTable("contacts")  -> this is not a viable method
// MAGIC # Accessing Data link is not available (https://community.cloud.databricks.com/#workspace/databricks_guide/03%20Accessing%20Data/0%20Accessing%20Data)
// MAGIC newDf.registerTempTable("contacts")

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Question #5 DataFrame UDFs and DataFrame SparkSQL Functions
// MAGIC 
// MAGIC Below we've created a small DataFrame. You should use DataFrame API functions and UDFs to accomplish two tasks.
// MAGIC 
// MAGIC 1. You need to parse the State and city into two different columns.
// MAGIC 2. You need to get the number of days in between the start and end dates. You need to do this two ways.
// MAGIC   - Firstly, you should use SparkSQL functions to get this date difference.
// MAGIC   - Secondly, you should write a udf that gets the number of days between the end date and the start date.

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC from pyspark.sql import functions as F
// MAGIC from pyspark.sql.types import *
// MAGIC 
// MAGIC # Build an example DataFrame dataset to work with. 
// MAGIC dbutils.fs.rm("/tmp/dataframe_sample.csv", True)
// MAGIC dbutils.fs.put("/tmp/dataframe_sample.csv", """id|end_date|start_date|location
// MAGIC 1|2015-10-14 00:00:00|2015-09-14 00:00:00|CA-SF
// MAGIC 2|2015-10-15 01:00:20|2015-08-14 00:00:00|CA-SD
// MAGIC 3|2015-10-16 02:30:00|2015-01-14 00:00:00|NY-NY
// MAGIC 4|2015-10-17 03:00:20|2015-02-14 00:00:00|NY-NY
// MAGIC 5|2015-10-18 04:30:00|2014-04-14 00:00:00|CA-SD
// MAGIC """, True)
// MAGIC 
// MAGIC formatPackage = "csv" if sc.version > '1.6' else "com.databricks.spark.csv"
// MAGIC df = sqlContext.read.format(formatPackage).options(header='true', delimiter = '|').load("/tmp/dataframe_sample.csv")
// MAGIC df.printSchema()

// COMMAND ----------

// MAGIC %python
// MAGIC # 1) parse the State and city into two different columns
// MAGIC from pyspark.sql.functions import split
// MAGIC col = split(df['location'], '-')
// MAGIC dfResult_1 = df.withColumn('State', col.getItem(0)).withColumn('City', col.getItem(1))
// MAGIC dfResult_1.show()

// COMMAND ----------

// MAGIC %python # 2) use SparkSQL to get the number of days in between the start and end dates
// MAGIC from pyspark.sql.functions import datediff, to_date
// MAGIC 
// MAGIC dfResult_2 = df.withColumn("diff_date", datediff( to_date(df.end_date), to_date(df.start_date) ))
// MAGIC dfResult_2.show()

// COMMAND ----------

// MAGIC %python # 3) use a UDF to get the number of days in between the start and end dates
// MAGIC from pyspark.sql.functions import udf, concat
// MAGIC from pyspark.sql.types import IntegerType
// MAGIC from datetime import datetime
// MAGIC 
// MAGIC def get_datediff(dfCol):
// MAGIC   end = (dfCol.split("_")[0]).strip()
// MAGIC   start = dfCol.split("_")[1] 
// MAGIC   end_dt = datetime.strptime(end, '%Y-%m-%d %H:%M:%S')
// MAGIC   start_dt = datetime.strptime(start, '%Y-%m-%d %H:%M:%S')
// MAGIC   diff = (end_dt-start_dt).days
// MAGIC   return diff
// MAGIC   
// MAGIC udf_datediff = udf(lambda dfCol: get_datediff(dfCol), IntegerType())
// MAGIC 
// MAGIC 
// MAGIC dfPrep = df.withColumn("comb_dates", concat(df.end_date, lit("_"),  df.start_date)) 
// MAGIC dfResult_3 = dfPrep.withColumn("dtdiff", udf_datediff("comb_dates").alias("dtdiff") )
// MAGIC dfResult_3.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ###Machine Learning
// MAGIC 
// MAGIC The following examples involve using MLlib algorithms

// COMMAND ----------

// MAGIC %md #### **Question 1:** Demonstrate The Use of a MLlib Algorithm Using the DataFrame Interface(`org.apache.spark.ml`).
// MAGIC 
// MAGIC Demonstrate use of an MLlib algorithm and show an example of tuning the algorithm to improve prediction accuracy.

// COMMAND ----------

// MAGIC %python
// MAGIC import requests
// MAGIC 
// MAGIC # Get the data and prepare with schema
// MAGIC URL_DATA = "https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.data"
// MAGIC URL_NAMES = "https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.names"
// MAGIC DST_DATA = "/dbfs/tmp/adult.csv"
// MAGIC DST_NAMES = "/dbfs/tmp/adult_names.csv"
// MAGIC r = requests.get(URL_DATA, allow_redirects=True)
// MAGIC open(DST_DATA, 'wb').write(r.content)
// MAGIC r = requests.get(URL_NAMES, allow_redirects=True)
// MAGIC open(DST_NAMES, 'wb').write(r.content)

// COMMAND ----------

println(dbutils.fs.head("/tmp/adult.csv"))

// COMMAND ----------

// MAGIC %python
// MAGIC import pyspark.sql.types as typ
// MAGIC 
// MAGIC names = sc.textFile(DST_NAMES[6:])
// MAGIC names_rm_top = names.zipWithIndex().filter(lambda x: x[1] > 95).map(lambda x: x[0])
// MAGIC names_col = names_rm_top.map(lambda x: ((x.split(":"))[0], ((x.split(":"))[1]).strip())  )
// MAGIC 
// MAGIC def check_type(tup):
// MAGIC   if tup[1]=='continuous.':
// MAGIC     rslt = (tup[0], typ.DoubleType())
// MAGIC   else:
// MAGIC     rslt = (tup[0], typ.StringType())
// MAGIC   return rslt
// MAGIC 
// MAGIC #check_type( names_col.take(1)[0] )
// MAGIC   
// MAGIC labels = names_col.map(lambda x: check_type(x)).collect()
// MAGIC labels = labels + [(u'income', typ.StringType())]

// COMMAND ----------

// MAGIC %python
// MAGIC schema = typ.StructType([
// MAGIC   typ.StructField(v[0], v[1], True) for v in labels
// MAGIC ])
// MAGIC 
// MAGIC dfDataRaw = sqlContext.read.csv(DST_DATA[6:], header=False, schema=schema)

// COMMAND ----------

// MAGIC %python
// MAGIC dfDataRaw.count()

// COMMAND ----------

// MAGIC %python # Transformers to convert strings to numeric vectors
// MAGIC from pyspark.ml.feature import StringIndexer, OneHotEncoderEstimator
// MAGIC 
// MAGIC categoryVar = [x[0] for x in labels if x[1]==typ.StringType()]
// MAGIC indexer = [StringIndexer(inputCol=X, outputCol=X+"Index").fit(dfDataRaw) for X in categoryVar]
// MAGIC 
// MAGIC inputs = [X+"Index" for X in categoryVar]
// MAGIC outputVec = [X+"Vec" for X in categoryVar]

// COMMAND ----------

// MAGIC %python
// MAGIC import pyspark.ml.feature as ft
// MAGIC 
// MAGIC encoder = ft.OneHotEncoderEstimator(
// MAGIC   inputCols=inputs, 
// MAGIC   outputCols=outputVec
// MAGIC )

// COMMAND ----------

// MAGIC %python # Prepare for sampling
// MAGIC from pyspark.ml import Pipeline
// MAGIC 
// MAGIC stages = indexer + [encoder]
// MAGIC pipeline = Pipeline(stages=stages)
// MAGIC dfDataClean = pipeline.fit(dfDataRaw).transform(dfDataRaw)

// COMMAND ----------

// MAGIC %python
// MAGIC dfDataClean.count()

// COMMAND ----------

// MAGIC %python # Split into training and testing
// MAGIC dfTrain, dfTest = dfDataClean.randomSplit([0.7, 0.3], seed=2)
// MAGIC print("Training set: {}".format(dfTrain.count()) )
// MAGIC print("Testing set: {}".format(dfTest.count()) )

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC numericCols = [col[0] for col in labels[:-1] if col[1]==typ.DoubleType()]
// MAGIC inputColumns = numericCols + outputVec[:-1]
// MAGIC 
// MAGIC featureVector = ft.VectorAssembler(
// MAGIC   inputCols=inputColumns,
// MAGIC   outputCol='features')

// COMMAND ----------

// MAGIC %python # Estimator
// MAGIC import pyspark.ml.classification as cl
// MAGIC 
// MAGIC logistic = cl.LogisticRegression(
// MAGIC     maxIter=10, 
// MAGIC     regParam=0.01, 
// MAGIC     labelCol='incomeIndex')

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.ml import Pipeline
// MAGIC 
// MAGIC # Now, out data is prepared and split into training, testing.
// MAGIC # We can use this to train and test the logistic regression model
// MAGIC stages = [featureVector] + [logistic]
// MAGIC pipeline = Pipeline(stages=stages)
// MAGIC model = pipeline.fit(dfTrain)
// MAGIC test_model = model.transform(dfTest)

// COMMAND ----------

// MAGIC %python # Evaluate model
// MAGIC import pyspark.ml.evaluation as ev
// MAGIC 
// MAGIC evaluator = ev.BinaryClassificationEvaluator(
// MAGIC   rawPredictionCol='probability',
// MAGIC   labelCol='incomeIndex')
// MAGIC 
// MAGIC print("Area under the ROC Curve")
// MAGIC print(evaluator.evaluate(test_model, 
// MAGIC      {evaluator.metricName: 'areaUnderROC'}))
// MAGIC print("Area under Precision Recall Curve")
// MAGIC print(evaluator.evaluate(test_model, {evaluator.metricName: 'areaUnderPR'}))

// COMMAND ----------

// MAGIC %python # Model Tuning using grid method
// MAGIC import pyspark.ml.tuning as tune
// MAGIC 
// MAGIC # model
// MAGIC logistic = cl.LogisticRegression(
// MAGIC     labelCol='incomeIndex')
// MAGIC 
// MAGIC # parameter grid
// MAGIC grid = tune.ParamGridBuilder() \
// MAGIC     .addGrid(logistic.maxIter,  
// MAGIC              [2, 10, 50]) \
// MAGIC     .addGrid(logistic.regParam, 
// MAGIC              [0.01, 0.05, 0.3]) \
// MAGIC     .build()
// MAGIC 
// MAGIC # evaluator
// MAGIC evaluator = ev.BinaryClassificationEvaluator(
// MAGIC     rawPredictionCol='probability', 
// MAGIC     labelCol='incomeIndex')
// MAGIC 
// MAGIC # cross validation frame
// MAGIC cv = tune.CrossValidator(
// MAGIC     estimator=logistic, 
// MAGIC     estimatorParamMaps=grid, 
// MAGIC     evaluator=evaluator
// MAGIC )
// MAGIC 
// MAGIC # transforming pipeline
// MAGIC pipeline = Pipeline(stages=[featureVector])
// MAGIC data_transformer = pipeline.fit(dfTrain)

// COMMAND ----------

// MAGIC %python # find best model (may take some time...)
// MAGIC cvModel = cv.fit(data_transformer.transform(dfTrain))

// COMMAND ----------

// MAGIC %python # evaluate new model and compare (insignificant difference)
// MAGIC data_train = data_transformer.transform(dfTest)
// MAGIC results = cvModel.transform(data_train)
// MAGIC 
// MAGIC print(evaluator.evaluate(results, 
// MAGIC      {evaluator.metricName: 'areaUnderROC'}))
// MAGIC print(evaluator.evaluate(results, 
// MAGIC      {evaluator.metricName: 'areaUnderPR'}))

// COMMAND ----------

// MAGIC %python # parameter values
// MAGIC results = [
// MAGIC     (
// MAGIC         [
// MAGIC             {key.name: paramValue} 
// MAGIC             for key, paramValue 
// MAGIC             in zip(
// MAGIC                 params.keys(), 
// MAGIC                 params.values())
// MAGIC         ], metric
// MAGIC     ) 
// MAGIC     for params, metric 
// MAGIC     in zip(
// MAGIC         cvModel.getEstimatorParamMaps(), 
// MAGIC         cvModel.avgMetrics
// MAGIC     )
// MAGIC ]
// MAGIC 
// MAGIC sorted(results, 
// MAGIC        key=lambda el: el[1], 
// MAGIC        reverse=True)[0]

// COMMAND ----------

// MAGIC %md 
// MAGIC I hope you enjoyed this time as much as I did.  What a wonderful story!

// COMMAND ----------

// MAGIC %md
// MAGIC The End
