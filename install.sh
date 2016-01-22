#!/bin/bash

protocol=""
install_only=""

while getopts t:p:o opt; do
  case $opt in
  p)
      protocol=$OPTARG
      ;;
  o)
      install_only=1
      ;;
  esac
done

if [ "$protocol" = "" ]; then
	protocol="usb websocket merchantkeypad"
fi

#echo "protocol=$protocol"

install() {
	echo "installing $1 to $d ..."
	adb -s $d install -r $1
}

if [ -z "$RP_DEVICES" ]; then
	echo "Missing RP_DEVICES property, set to space separated list of devices attached via ADB"
	exit 1
fi

if [ -z "$install_only" ]; then
	gradle assembleDebug
	r=$?
	if [ $r -ne 0 ]; then
		exit $r
	fi
fi

for d in $RP_DEVICES; do
	install ./remote-terminal-kiosk/build/outputs/apk/remote-terminal-kiosk-debug.apk
	for p in $protocol; do
		if [ $p = "usb" ]; then
			install ./remote-protocol-usb/build/outputs/apk/remote-protocol-usb-debug.apk
		elif [ $p = "merchantkeypad" ]; then
			install ./remote-protocol-merchantkeypad/build/outputs/apk/remote-protocol-merchantkeypad-debug.apk
		fi
	done
done

