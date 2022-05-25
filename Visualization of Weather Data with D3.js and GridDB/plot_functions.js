import { histogram } from './helpers/hist.js';
import { boxplot } from './helpers/box.js';
import { tables } from './helpers/tab.js';
import { correlogram } from './helpers/correl.js';
import { lineplot } from './helpers/line.js';


const countNotNull = (array) => {
    let count = 0
    for(let i = 0; i < array.length; i++){
        if(isNaN(parseInt(array[i]))==false && array[i]!==null){   
            count++
        }
    }
    return count
 
};

const displayHisto = (data) => {
    let result = data.flatMap(Object.keys);
    result = [...new Set(result)];
    
    tables("data_table",data,result,true)
    
    let obj = {}
    
    let summary_data = []
    for(let i = 0; i < result.length; i++){
        
        obj[result[i]]  = data.map(function(elem){return elem[result[i]]});
        let count = 0
        let summarize = {}

        if(result[i]!='DATE'){
            obj[result[i]] = obj[result[i]].map(Number)
        }
        summarize['Field'] = result[i]
        summarize['Max'] = d3.max(obj[result[i]])
        summarize['Min'] = d3.min(obj[result[i]])
        summarize['Deviation'] = d3.deviation(obj[result[i]])
        summarize['Mean'] = d3.mean(obj[result[i]])
        summarize['Median'] = d3.median(obj[result[i]])
        
        summarize['Count Not Null'] = countNotNull(obj[result[i]])
        
        summary_data.push(summarize)
    }
    let result2 = summary_data.flatMap(Object.keys)
    result2 = [...new Set(result2)];
    tables("data_table2",summary_data,result2,false)

    // console.log(obj)
    console.log(summary_data)
    histogram("my_dataviz",obj['WIND'],'Average Wind Speed')
    boxplot("my_dataviz",obj['WIND'],"Wind Speed")
    
    histogram("my_dataviz2",obj['RAIN'],'Precipitation (Rain)',"orange")
    boxplot("my_dataviz2",obj['RAIN'],"Precipitation (Rain)",'brown')

    histogram("my_dataviz3",obj['T.MAX'],'Max Temperature',"green")
    boxplot("my_dataviz3",obj['T.MAX'],"Max Temperature",'yellow')


    histogram("my_dataviz4",obj['T.MIN'],'Min Temperature',"purple")
    boxplot("my_dataviz4",obj['T.MIN'],"Min Temperature",'pink')


    histogram("my_dataviz5",obj['T.MIN.G'],'Minimum Grass Temperature',"cyan")
    boxplot("my_dataviz5",obj['T.MIN.G'],"Minimum Grass Temperature",'orange')

    histogram("my_dataviz6",obj['IND'],'Indicator')
    // boxplot("my_dataviz6",obj['IND'],"Indicator")


    histogram("my_dataviz7",obj['IND.1'],'Indicator 1',"orange")
    // boxplot("my_dataviz7",obj['IND.1'],"Indicator 1",'brown')

    histogram("my_dataviz8",obj['IND.2'],'Indicator 2',"green")
    // boxplot("my_dataviz8",obj['IND.2'],"Indicator 2",'yellow')


    lineplot("my_dataviz9",data,obj,'DATE','WIND','#4169e1')
    lineplot("my_dataviz9",data,obj,'DATE','RAIN','#4169e1')
    lineplot("my_dataviz9",data,obj,'DATE','T.MAX','#4169e1')
    lineplot("my_dataviz9",data,obj,'DATE','T.MIN','#4169e1')
    lineplot("my_dataviz9",data,obj,'DATE','T.MIN.G','#4169e1')
    
    correlogram(data)
      
}


d3.csv("./out.csv", displayHisto)
