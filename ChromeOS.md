Installing and using GridDB on ChromeOS machines can be accomplished by setting up Linux on your Chromebook. There is an official list of devices which can utilize Linux on ChromeOS, though essentially every device released in 2019 or afterwards should be able to add Linux; if your device is from prior to 2019, there is an official list here: [https://sites.google.com/a/chromium.org/dev/chromium-os/chrome-os-systems-supporting-linux](https://sites.google.com/a/chromium.org/dev/chromium-os/chrome-os-systems-supporting-linux).

So, in order to utilize GridDB on a Chromebook, you will need to enable Linux and then we can install GridDB in the same method as we do on other Linux machines.

## Linux on ChromeOS

To enable Linux, head to your chromebook's settings, find the advanced section, and then developers. You will see a setting titled "Linux development environment" and select "turn on". You will then be greeted with some simply on-screen instructions and ChromeOS will handle all of the installation for you.

Once you restart, you will have access to a Linux command line based on Debian (similar to Ubuntu). From here, you will be able to use the APT package manager to install packages in your Linux environment. And similar to [WSL](), this instance of Linux living inside your ChromeOS machine is a bit similar to a virtual machine; it is a container which runs inside your OS and can be accessed from within your ChromeOS environment. 

## Install GridDB 

As stated before, you now have access to the package manager known as APT, which means you can install GridDB directly through that as seen here: [https://docs.griddb.net/gettingstarted/using-apt/](https://docs.griddb.net/gettingstarted/using-apt/)

```bash
$ sudo sh -c 'echo "deb https://www.griddb.net/apt griddb/5.1 multiverse" >>  /etc/apt/sources.list.d/griddb.list'
$ wget -qO - https://www.griddb.net/apt/griddb.asc | sudo apt-key add -
```

And then 

```bash 
$ sudo apt update
$ sudo apt install griddb-meta
```

The meta package for GridDB will install GridDB, the [GridDB C-client](https://github.com/griddb/c_client), the [GridDB CLI](https://github.com/griddb/cli), and the [GridDB JDBC Driver](https://github.com/griddb/jdbc) -- how convenient! Once GridDB is installed, you can start it up: `$ sudo systemctl start gridstore`

## Conclusion 

And that's really all there is to it. You can now use GridDB in whatever capacity you like all within your currently running ChromeOS enviornment.
