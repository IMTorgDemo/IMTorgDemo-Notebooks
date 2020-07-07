#!/usr/bin/env python3
"""
PURPOSE: This script performs operations to complete Task-2,3,4.  This 
includes
* option-1 transform
* option-2 transform
* create sqlite3.db and table
* insert rows

USAGE: 
```
pipenv install 
pipenv shell
python script-Task2.py
```



TODO:
* better documentation
* cmdln args for generalizing to other files
* error handling
* unit tests

"""

__author__ = "Jason Beach"
__version__ = "0.1.0"
__license__ = "MIT"


import os
import pandas as pd
import numpy as np
import sqlite3





def option1(option1_files):
    #schema
    schema = pd.read_csv(  option1_files['file_schema']  )
    fix_widths = schema['Column Width'].values.tolist()
    col_names = schema['Name'].values.tolist()
    col_names_lwr = [x.lower() for x in col_names]

    #import and transform
    df_input = pd.read_fwf(  option1_files['file_input'], 
                    widths = fix_widths,
                    names = col_names_lwr )
    df_validate = pd.read_csv(  option1_files['file_output_validate']  )
    test_validate = 'PASS' if df_input.equals(df_validate) else 'FAIL'
    print(f"option-1, validation test: {test_validate}")

    #export
    file_output = "./results/result-task2_option1.csv"
    df_input.to_csv(file_output, index=False)
    print(f"option-1, output to: {file_output}")


def option2(option2_files):
    #schema
    schema = pd.read_csv(  option2_files['file_schema']  )
    pd.options.mode.chained_assignment = None
    schema.iloc[schema.shape[0]-1: schema.shape[0]:]['Name'] = 'blank'   #ignore copy warnings

    #indexing logic
    def option2A_logic(index):
        if index % 2 != 0:
            return True
        return False

    def option2B_logic(index):
        if index % 2 == 0:
            return True
        return False

    #prepare dfA
    df_tmp = schema[schema.CFI.isin(['A','A and B'])]
    fix_widths = df_tmp['Width'].values.tolist()
    col_names_lwr = [x.lower() for x in df_tmp['Name'].values.tolist()]

    dfA = pd.read_fwf(  option2_files['file_input'], 
                    widths = fix_widths,
                    names = col_names_lwr,
                    skiprows= lambda x: option2A_logic(x) )

    #prepare dfB
    df_tmp = schema[schema.CFI.isin(['B','A and B'])]
    fix_widths = df_tmp['Width'].values.tolist()
    col_names_lwr = [x.lower() for x in df_tmp['Name'].values.tolist()]

    dfB = pd.read_fwf(  option2_files['file_input'], 
                    widths = fix_widths,
                    names = col_names_lwr,
                    skiprows= lambda x: option2B_logic(x) )


    dfA.drop(columns=['cfi'],inplace=True)
    dfB.drop(columns=['cfi','blank'],inplace=True)
    dfC = dfA.merge(dfB, on=['pccn','plisn'], how='outer')
    df_validate = pd.read_csv(  option2_files['file_output_validate']  )
    test_validate = 'PASS' if dfC.equals(df_validate) else 'FAIL'
    print(f"option-2, validation test: {test_validate }")

    file_output = "./results/result-task2_option2.csv"
    dfC.to_csv(file_output, index=False)
    print(f"option-2, output to: {file_output}")
    return dfC


def task34(dfC):
    convert = {
        str(np.dtype(object)):'text',
        str(np.dtype(float)):'real'
    }
    cols = list(zip( [str(x) for x in dfC.dtypes.to_list()], dfC.columns.to_list() ) )

    #create db and connection
    file_db = "./results/result.db"
    if os.path.isfile(file_db):
        os.remove(file_db)
    conn = sqlite3.connect(file_db)
    cur = conn.cursor()
    print(f"task-3,4 connection made to: {file_db}")

    #create table
    tbl_name = "task2"
    colname_type = [x[1] + ' ' + convert[x[0]] for x in cols]
    cmd = "create table " + tbl_name + " (" + ', '.join(colname_type) + ")"
    cur.execute(cmd)
    conn.commit()
    print(f"task-3,4 created table: {tbl_name}")

    #append data to table
    dfC.to_sql('task2', conn, if_exists='replace', index = False)
    conn.close()
    print("task-3,4 connection to db closed")









def main():
    """ Main entry point of the app """
    dir = './Systecon_Practial_test_-_Back-end/Back-End_Task_Data/LsaParsing'
    files = os.listdir(dir)

    #option-1
    option1_files = {
        'file_schema': os.path.join(dir,files[3]),
        'file_input': os.path.join(dir,files[4]),
        'file_output_validate': os.path.join(dir,files[0])
        }
    option1(option1_files)

    #option-2
    option2_files = {
        'file_schema': os.path.join(dir,files[1]),
        'file_input':os.path.join(dir,files[2]),
        'file_output_validate': os.path.join(dir,files[5])
    }
    dfC = option2(option2_files)

    #option-3,4
    task34(dfC)
    print("script completed")





if __name__ == "__main__":
    """ This is executed when run from the command line """
    main()
