from kivy.app import App

from kivy.uix.screenmanager import NoTransition, ScreenManager, Screen
from kivy.uix.image import Image
from kivy.clock import Clock
from kivy.uix.boxlayout import BoxLayout

from datetime import datetime, timedelta
import pandas as pd
import time
import seaborn as sns
import matplotlib.pyplot as plt

import os

global layout
layout = BoxLayout(orientation='horizontal')

ChartScr = Screen()
ChartScr.add_widget(layout)
sm = ScreenManager()
sm.add_widget(ChartScr)


class GridDBApp(App):

    def build(self):
        return sm

    def renderPng(self):

        # new query with a .CSV file
        iotdf = pd.read_csv('/Users/yulialukashina/Downloads/iotdf.csv')
        iotdf['timestamp']= iotdf['timestamp'].apply(pd.to_datetime)
        iotdf['simid'] = iotdf['simid'].apply(lambda x: '000' + str(x))
        #iotdf = iotdf[iotdf['timestamp'] > (datetime.now() - timedelta(minutes = 20))]
        
        # as in the previous article
        pivotdf = iotdf.pivot_table(index=['timestamp', 'simid', 'data_usage'],
                                        columns='event', 
                                        values= 'event',
                                        aggfunc=lambda x: 1)
        pivotdf = pivotdf.reset_index()    
        pivotdf = pivotdf.fillna(0)
        
        #data visualization
        order = ['0001', '0002', '0003', '0004']

        
        
        #data usage
        g = sns.relplot(
        data=pivotdf,
        x="timestamp", y="data_usage", col="simid", hue="simid",col_order = ['0001', '0002', '0003', '0004'],
        kind="line", palette="Set2", linewidth=4, zorder=5,
        col_wrap=1, height=2, aspect=7, legend=False,
        )

        for simid, ax in g.axes_dict.items():


            ax.text(.8, .85, simid, transform=ax.transAxes, fontweight="bold")


            sns.lineplot(
                data=pivotdf, x="timestamp", y="data_usage", units="simid",
                estimator=None, color=".7", linewidth=1, ax=ax,
            )

        ax.set_xticks(ax.get_xticks()[::1])
        g.set_xticklabels(rotation=90)

        g.set_titles("")
        g.fig.suptitle('DATA USAGE                   ', horizontalalignment = 'right')
        
        g.savefig('png1.png')
        
        #number of pdp events
        g = sns.relplot(
        data=pivotdf,
        x="timestamp", y="Create PDP context", col="simid", hue="simid",col_order = ['0001', '0002', '0003', '0004'],
        kind="line", palette="Set2", linewidth=4, zorder=5,
        col_wrap=1, height=2, aspect=7, legend=False,
        )

        for simid, ax in g.axes_dict.items():


            ax.text(.8, .85, simid, transform=ax.transAxes, fontweight="bold")


            sns.lineplot(
                data=pivotdf, x="timestamp", y="Create PDP context", units="simid",
                estimator=None, color=".7", linewidth=1, ax=ax,
            )

        ax.set_xticks(ax.get_xticks()[::1])
        g.set_xticklabels(rotation=90)

        g.set_titles("")
        g.fig.suptitle('NUMBER OF PDP EVENTS                   ', horizontalalignment = 'right')
        
        g.savefig('png2.png')
        
        #alerts
        g = sns.relplot(
        data=pivotdf,
        x="timestamp", y="alert", col="simid", hue="simid",col_order = ['0001', '0002', '0003', '0004'],
        kind="line", palette="Set2", linewidth=4, zorder=5,
        col_wrap=1, height=2, aspect=7, legend=False,
        )

        for simid, ax in g.axes_dict.items():


            ax.text(.8, .85, simid, transform=ax.transAxes, fontweight="bold")


            sns.lineplot(
                data=pivotdf, x="timestamp", y="alert", units="simid",
                estimator=None, color=".7", linewidth=1, ax=ax,
            )

        ax.set_xticks(ax.get_xticks()[::1])
        g.set_xticklabels(rotation=90)

        g.set_titles("")
        g.fig.suptitle('NUMBER OF ALERTS                      ', horizontalalignment = 'right')
        
        g.savefig('png3.png')

        print('saved pictures')

    Clock.schedule_interval(renderPng, 10)

    def clearLayout(self):
        layout.clear_widgets()
    Clock.schedule_interval(clearLayout, 10)

    def buildLayout(self):

        for i in range(1,4):
            layout.add_widget(Image(source = 'png' + str(i) + '.png'))
            print('replaced pictures')

        try:
            for i in range(1,4):
                os.remove('png' + str(i) + '.png')
                print('done')
        except:
            pass

    Clock.schedule_interval(buildLayout, 10)


GridDBApp().run()
