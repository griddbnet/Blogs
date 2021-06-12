python3.6 -m pip install pycoingecko


from pycoingecko import CoinGeckoAPI


install matplotlib.pylot as plt
import pandas as pd
import numpy as np
import scipy as sp


coingecko = CoinGeckoAPI()



def get_data(cryptocurrency):
    cryptocurrency_data = coingecko.get_coin_by_id(cryptocurrency, market_data='true', sparkline='true')
    df = pd.DataFrame.from_dict(cryptocurrency_data, orient='index')
    df.to_csv(r'cryptocurrency_data.csv')
    return df


get_data('bitcoin')


get_data('ethereum')


def get_historical_data(cryptocurrency, fiat_currency, number_of_days):
    historic_price = coingecko.get_coin_market_chart_by_id(cryptocurrency, fiat_currency, number_of_days)
    prices = [price[1] for price in historic_price['prices']]
    return prices
    
    
    print(get_historical_data('bitcoin', 'USD', 5))
    
    
    ethereum = get_historical_data('ethereum', 'USD', 30)
    
    
    et_df = pd.DataFrame(ethereum, columns = ['Ethereum Value'])
    et_df.index.name = 'Every Hour Count in the Past 30 Days'
    et_df.to_csv(r'Ethereum.csv')
    df = pd.read_csv("Ethereum.csv")
    
    
    factory = griddb.StoreFactory.get_instance()
    argv = sys.argv
    try:
	#Get GridStore object
	gridstore = factory.get_store(
        notification_member="griddb-server:10001",
        cluster_name="defaultCluster",
        username="admin",
        password="admin"
    )
    
    ethereum_data = pd.read_csv("/ethereum.csv", na_values=['\\N'])
    ethereum_data.info()
    
    
    ethereum_container = "ETHEREUM"
    ethereum_containerInfo = griddb.ContainerInfo(ethereum_container,
	[["count", griddb.Type.INTEGER],["value", griddb.Type.STRING]],
	griddb.ContainerType.COLLECTION, True)
    ethereum_columns = gridstore.put_container(ethereum_containerInfo)
	ethereum_columns.create_index("count", griddb.IndexType.DEFAULT)
    ethereum_columns.put_rows(ethereum_data)	
    print("Data added successfully")	
    db_data = gridstore.get_container(ethereum_container)
    
    
    query = db_data.query("Select *")
    rs = query.fetch(False)
    print(f"{ethereum_container} Data")
    retrieved_data = []
    while rs.has_next():
        data = rs.next()
        retrieved_data = append(newdata)
        print(retrieved_data)
        
    query = db_data.query("select * where count = 7")
    rs = query.fetch(False)
    print(f"{ethereum_container} Data")
    retrieved_data = []
    while rs.has_next():
        data = rs.next()
        retrieved_data = append(newdata)
        print(retrieved_data)
        
    
    query = db_data.query("select * where count >= 600")
    rs = query.fetch(False)
    print(f"{ethereum_container} Data")
    retrieved_data = []
    while rs.has_next():
        data = rs.next()
        retrieved_data = append(newdata)
        print(retrieved_data)

    query = db_data.query("select * where value > 2400")
    rs = query.fetch(False)
    print(f"{ethereum_container} Data")
    retrieved_data = []
    while rs.has_next():
        data = rs.next()
        retrieved_data = append(newdata)
        print(retrieved_data)
        
    query = db_data.query("select * where value >2000")
    rs = query.fetch(False)
    print(f"{ethereum_container} Data")
    retrieved_data = []
    while rs.has_next():
        data = rs.next()
        retrieved_data = append(newdata)
        print(retrieved_data)
        
    except griddb.GSException as e:
	for i in range(e.get_error_stack_size()):
		print("[", i, "]")
		print(e.get_error_code(i))
		print(e.get_location(i))
		print(e.get_message(i))
		
	df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Ethereum Value', kind = 'scatter')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Ethereum Value')
	plt.show()
	
	df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Ethereum Value', kind = 'line')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Ethereum Value')
	plt.show()
	
	df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Ethereum Value', kind = 'bar')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Ethereum Value')
	plt.show()
	
	
	import statsmodel.api as sm
	sm.graphics.tsa.plot_acf(df["Ethereum Value"])
	plt.show()
	
	
    bitcoin = get_historical_data('bitcoin', 'USD', 30)
    
    bt_df = pd.DataFrame(bitcoin, columns = ['Bitcoin Value'])
    bt_df.index.name = 'Every Hour Count in the Past 30 Days'
    bt_df.to_csv(r'Bitcoin.csv')
    df = pd.read_csv("Bitcoin.csv")
    
    
    df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Bitcoin Value', kind = 'scatter')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Bitcoin Value')
	plt.show()
	
	
	df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Bitcoin Value', kind = 'line')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Bitcoin Value')
	plt.show()
	
	
	df.plot(x = 'Every Hour Count in the Past 30 Days', y = 'Bitcoin Value', kind = 'bar')
	plt.xlabel('Every Hour Count in the Past 30 Days')
	plt.ylabel('Bitcoin Value')
	plt.show()
	
	
	
	import statsmodel.api as sm
	sm.graphics.tsa.plot_acf(df["Bitcoin Value"])
	plt.show()