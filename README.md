This article is a direct continuation of a previous [effort]() in which we deployed a GridDB application to a local kubernetes (single node) cluster. We will take the knowledge gained from that deployment and use it to help us deploy that same cluster onto Microsoft's cloud computing services: [Azure](https://azure.microsoft.com/en-us). Because most of the work is already done, we will simply be going over the Azure-speicifc changes needed to get this project up and running on Azure.

First and foremost, you will of course need to make an Azure membership, and luckily setting up an [AKS]() cluster is free. Once you have an account, you can follow along with this article; if followed all the way through, you will have the simple todo app discussed in our previous blog hosted on Azure and accessible from wherever you have access to the internet.

## Getting Started

To begin, please create an account with Microsoft Azure and perhaps read the previous entries about this project. Also please clone the source code from GitHub to be able to use the provided .yaml files. You may also need to create a Dockerhub account to store your own images, though for now you can use the images I have pushed onto Dockerhub.

Another optional step is to install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) so that you can run Azure commands from your machine and not deal with using their cloud shell, but again, this is optional.

## Changes from Local Cluster to Azure

Let's first discuss the small amounts of changes needed to run this project on Azure's managed AKS cluster rather than a local K3s single node; first, the container images will need to be publically available on Dockerhub (or some other container image repository) for ease of use -- in our previous article, we were simply pushing our images onto a locally running registry. And then there's not much else to it -- the last difference will be in how we handle persistent storage.

## Creating an AKS Cluster

To begin, we are following along with the guide provided directly from the Microsoft team: [https://learn.microsoft.com/en-us/azure/aks/learn/quick-kubernetes-deploy-cli](https://learn.microsoft.com/en-us/azure/aks/learn/quick-kubernetes-deploy-cli). Following this, we will create our resource group, create our AKS cluster, and then connect to the cluster. From there, we can step in with our own containers.

Connect to either the Azure cloud shell or to your local Azure CLI and connect to your Azure account.

### Create Azure Resources

First let's create the resource group

```bash
$ az group create --name myResourceGroup --location westus
```

And then let's create the actual AKS cluster

```bash
$ az aks create -g myResourceGroup -n myAKSCluster --enable-managed-identity --node-count 1 --enable-addons monitoring --enable-msi-auth-for-monitoring  --generate-ssh-keys
```

And note, to keep this simple and free, I personally removed the addons for monitoring, but you can choose whether you'd like to keep it or not.

And now let's connect to our cluster by installing [kubectl](https://kubernetes.io/docs/reference/kubectl/) (not necessary if not using the Cloud Shell)

```bash
$ az aks install-cli
```

And connect it:

```bash
$ az aks get-credentials --resource-group myResourceGroup --name myAKSCluster
```

And test it: 

```bash
$ kubectl get nodes
```

    NAME                       STATUS   ROLES   AGE     VERSION
    aks-nodepool1-31718369-0   Ready    agent   6m44s   v1.12.8

And with that, we should be ready to directly add resources onto our Azure AKS cluster with our command line.

### Creating Kubernetes Resources

In your azure resource, git is already installed, so please clone the repo with all of the source code for this project: [https://github.com/griddbnet/Blogs/tree/kubernetes](https://github.com/griddbnet/Blogs/tree/kubernetes). The three unique directories correspond to the different containers/microservices we intend to run as contains in our AKS cluster. 

If you read our previous effort of running this project on a local instance of K3s, most of this will be familiar; almost no changes were necessary from our .yaml files. One of the few changes was to where we are pull the image from -- we are now pulling from Dockerhub rather than pushing to a local registry. 

```bash
    spec:
      containers:
        - name: griddbcontainer
          image: imru/griddb-server:latest
```

I have already tagged and pushed these images to Dockerhub for use, but if you are curious as to the process: 

```bash
$ docker build -t griddb-server .
$ docker tag griddb-server imru/griddb-server:latest
$ docker push imru/griddb-server:latest
```

If you make any changes to your image and would like to update your pods, you can simply push an updated image to dockerhub and delete your deployment and re-set it to create a new, updated pod.

So, basically you can go in to each directory and run the create command with kubectl to create our services.

```bash
$ kubectl create -f griddb-server.yaml
```

#### Persistent Storage on Azure

One thing mentioned at the top was that the persistent storage was changed. If you try to use the method of using the host machine as the persistent storage as we did with the local K3s cluster, your AKS deployment will fail. We will need to first create an azure disk and then use that as our persistent storage for our GridDB storage.

Because our application is very straight forward and small, we will utilizing static provision of volume on Azure as seen here: [Statically provision a volume](https://learn.microsoft.com/en-us/azure/aks/azure-csi-disk-storage-provision#statically-provision-a-volume).

You can follow along with the link above, but here are the commands needed to create a disk and then apply that storage to our GridDB pod.

First, let's grab the node resource group

```bash
$ az aks show --resource-group myResourceGroup --name myAKSCluster --query nodeResourceGroup -o tsv

# Output
MC_myResourceGroup_myAKSCluster_eastus
```

Once we have this, let's create the disk using our commands:  

```bash
$ az disk create \
  --resource-group MC_myResourceGroup_myAKSCluster_eastus \
  --name myAKSDisk \
  --size-gb 1 \
  --query id --output tsv
```

Here we are creating a small 1 GB disk and then it will output the disk resource ID which is needed for our yaml files: 

    /subscriptions/<subscriptionID>/resourceGroups/MC_myAKSCluster_myAKSCluster_eastus/providers/Microsoft.Compute/disks/myAKSDisk

And now we need to create our persisent volume, our persistent volume claim, and lastly attach to our griddb deployment yaml file.

let's create `pv-azuredisk.yaml`

```bash
apiVersion: v1
kind: PersistentVolume
metadata:
  annotations:
    pv.kubernetes.io/provisioned-by: disk.csi.azure.com
  name: pv-azuredisk
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: managed-csi
  csi:
    driver: disk.csi.azure.com
    readOnly: false
    volumeHandle: /subscriptions/<subscriptionID>/resourceGroups/MC_myAKSCluster_myAKSCluster_eastus/providers/Microsoft.Compute/disks/myAKSDisk
    volumeAttributes:
      fsType: ext4
```

And our pvc: `pvc-azuredisk.yaml `

You will notice that here the storageClassName is `managed-csi` rather than `standard`. This is what we want!

And create

```bash
$ kubectl apply -f pv-azuredisk.yaml
$ kubectl apply -f pvc-azuredisk.yaml
```

output

    NAME            STATUS   VOLUME         CAPACITY    ACCESS MODES   STORAGECLASS   AGE
    pvc-azuredisk   Bound    pv-azuredisk   1Gi        RWO                           5s

And then in our GridDB deployment yaml: 

```bash
      volumes:
        - name: azure
          persistentVolumeClaim:
            claimName: pvc-azuredisk
```

And in the container section of our yaml

```bash
      containers:
        - name: griddbcontainer
          image: imru/griddb-server:latest
          ports:
            - containerPort: 10001
          securityContext:
            runAsUser: 0
            runAsGroup: 0
          volumeMounts:
            - name: azure
              mountPath: /mnt/azure
```

If you delete your GridDB deployment and re-deploy it, it will now have a persistent storage on your Azure AKS

## Closing Thoughts

And with minimal effort, we have now migrated from a local instance of our application, onto a robust and powerful cloud-based cluster on Azure for little to no credits. And if you are new to Azure, you will be able to see your cluster in an online portal with all of your nodes, services, etc. 

![images/aks-services.png]

![images/aks-storage.png]
