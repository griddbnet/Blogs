This article aims to be seen as supplemental to the original [GridDB Cloud Quickstart Guide](https://griddb.net/en/blog/griddb-cloud-quick-start-guide/). In *that* article, we covered how to sign up for the FREE Trial version of the GridDB Cloud offering and then dived into usage via the GridDB Web API. 

In this one, we want to cover signing up for GridDB Cloud via the Azure Marketplace. There are two flavors of this: the Pay-As-You-Go Plan, and the Fixed Monthly Shared instance. We will cover signing up, the pricing, and differences with the Free trial version. Usage with the Web API will not be covered as it is identical to our previous efforts; you can also use the [GridDB CLI Tool](https://griddb.net/en/blog/griddb-cloud-cli/) to interface with these Cloud offerings as well.

## The Plans (and how to pick)

First and foremost, to use GridDB Cloud on Azure, you will need a Microsoft Azure account. You can sign up via their website: [https://azure.microsoft.com/en-us/pricing/purchase-options/azure-account](https://azure.microsoft.com/en-us/pricing/purchase-options/azure-account). 

Next, you will choose if you want to sign up for the plan which allows you to pay and scale as needed, or shoot for the fixed monthly commitment plan. 

- [Pay-As-You-Go](https://azuremarketplace.microsoft.com/en-us/marketplace/apps/2812187.griddb_cloud_payasyougo?tab=Overview)
- [Fixed Monthly Commitment](https://azuremarketplace.microsoft.com/en-us/marketplace/apps/2812187.griddb_cloud_shared_instance_with_1month_trial?tab=Overview)

![pay-as-you-go](/images/pay-as-you-go-plan.png)

![monthly](/images/monthly.png)

The main difference between these two instances is the pricing -- with the monthly commitment you're paying $520/month, but you won't need to worry about possibly going over budget if you're moving lots of data in and out or making tons of requests. 

The pricing is as follows for the other plan: 

| Service     | Price                   |
|-------------|-------------------------|
| Storage     | $0.002 per 1GB/hour     |
| Data Out    | $0.09 per 1GB           |
| Data In     | $0.0025 per 1MB         |
| Request     | $0.012 per 100 requests |

So let's try to do some quick napkin math and see what kind of scenario you'd need to be in to make the shared monthly $520 commitment the *right* choice.

### Rough Cost Estimations

Let's assume, for a baseline, that you're storing exactly half of the maximum amount (100GB). At the price of $0.002/1GB/hour, that puts us at roughly $72/month on storage costs. Working from here, let's estimate how many data transfers we need to commit to reach our soft limit.

To spend the rest of the $448 budget, let's take a look at some scenarios. We'd need to commit ~5000GB of data through Data Out to reach our allotment. For Data In, it's about 175GB (it costs a lot more to do this!). 

If we wanted to do an even split between Data In and Data Out ($224 each), it'd be 2488.88GB for Data Out, 87.50GB for Data In. Requests cost are about 1,000,000 for $120, so accounting for that, we'd end up with this kind of scenario:

| Service Component | Details / Volume                      | Cost      |
|-------------------|---------------------------------------|-----------|
| Storage           | 50 GB (for 720 hours)                 | $72.00    |
| Requests          | 1,000,000 requests                    | $120.00   |
| Data Out          | Approx. 1822.22 GB                    | $164.00   |
| Data In           | 65,600 MB (or approx. 64.06 GB)       | $164.00   |
| **Total** |                                       | **$520.00** |

If your project grows to this size, it is of course recommended to commit to the $520 plan, but before that, you can comfortably use the Pay-As-You-Go Plan.


## Signing Up

Now that you know which plan best suits you, let's walk through that process. As mentioned above, you will need an Azure account. And now simply click on the plan you want and click Get It Now. From there, sign in. You will be greeted with a permissions ask. You must accept to continue.

![images](/images/permissions-0.png)


And once you accept, you are greeted with the subscribe page

![subscribe](/images/pay-as-you-go-subscribe.png)

Click Subscribe to fill out your normal Azure details like resource group (you can create a new one here) and the name of your service (this is just for internal use, it can be whatever you want). Once you fill it out, just hit Review + subscribe

![azure-signing-up](/images/azure-signing-up.png)

And once Azure provisions your instance, you are not *quite* done as you will also need to click `configure account now`, which will link you to GridDB Cloud's subscription page. Once you sign up there, it will link back to your Azure account.

And please note, in this portion, my browser was actively hiding all pop ups from the page and so I encountered an error like so: 

![error](/images/possible-error.png)

To get around it, find where your browser is blocking the pop up (it should be immediately easy to see), and then click allow. It will then ask for some Azure -> GridDB Cloud permissions to be accepted. 

![permissions-2](/images/azure-permissions.png)

Once accepted, your subscription will begin being fulfilled:

![pending](/images/pending-fulfillment.png)

And then once finished:

![finished](/images/finished.png)

And now, in that GridDB Cloud splash page, you will have access to your GridDB Cloud information, such as the management gui URL, as well as the user credentials. From this point on, you can now fully switch to the [GridDB Cloud Quickstart Guide](https://griddb.net/en/blog/griddb-cloud-quick-start-guide/) which was linked above to get started. You can also feel free to use the [GridDB CLI Tool](https://griddb.net/en/blog/griddb-cloud-cli/) as an easier way to interface with your instance from your shell. 

And though the instructions for this process was shown for the Pay-As-You-Go plan, the steps are the exact same for Monthly Commitment Plan

## Conclusion

And with that, we have shown how easy it is to sign up for the new Azure-based GridDB Cloud shared instance and similar of a process usage will be! We also did some rough  math to estimate which plan might be best for you and your stage of data. 