"""
  Usage:
  1. Go to ../sichu/
  2. run: python ../tools/autobuild.py
  3. (cmd) markets # show all markets
  4. (cmd) build {market id} {version} # build specific market apk
"""

from cmd import Cmd
from datetime import datetime

import xml.etree.ElementTree as ET
import os

NAMESPACE = 'http://schemas.android.com/apk/res/android'
MANIFEST = '../sichu/AndroidManifest.xml'

ET.register_namespace('android', NAMESPACE)


def shell_cmd(cmd):
    pip = os.popen(cmd)
    print ''.join(pip.readlines())
    pip.close()

def get_apk_filename(market, version):
    now = datetime.now()
    return "micabinet_%s_%s_%s.apk" % (version, market, now.strftime('%Y%m%d%H%M'))


class APKBuilder(Cmd):
    """
    01 sichu
    02 mumayi
    03 xiaomi
    04 anzhi
    05 hiapk
    06 myapp
    """
    markets = ['sichu', 'mumayi', 'xiaomi', 'anzhi', 'hiapk', 
               'myapp']
    
    def __init__(self):
        Cmd.__init__(self)

    def do_quit(self, line):
        exit(0)
    
    def do_markets(self, line):
        print 80 * '*'
        print "Target markets:"
        for idx, m in enumerate(APKBuilder.markets):
            print idx + 1, ':', m

    def do_build(self, line):
        idx, version, pwd = line.split()
        idx = int(idx) - 1
        tree = ET.parse(MANIFEST)
        root = tree.getroot()
        meta = root.findall('./application/meta-data')
        for i in meta:
            if i.get('{%s}name' % NAMESPACE) == 'UMENG_CHANNEL':
                i.set('{%s}value' % NAMESPACE, APKBuilder.markets[idx])
                tree.write(MANIFEST)
                # build
                print "building apk...\nant release"
                shell_cmd("ant release")
                # sign
                print "signing apk...\n"
                shell_cmd("jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore ../tools/sichu.keystore -storepass %s -signedjar bin/SplashActivity-release-signed.apk bin/SplashActivity-release-unsigned.apk sichu-android" % pwd)
                # verify signed apk
                print "verify signing...\n"
                shell_cmd("jarsigner -verify bin/SplashActivity-release-signed.apk")
                # zipalign apk
                print "zipalign apk...\n"
                apk = get_apk_filename(APKBuilder.markets[idx], version)
                shell_cmd("zipalign -v 4 bin/SplashActivity-release-signed.apk ../tools/%s" % apk)
                # reset manifest file
                shell_cmd("git checkout -- AndroidManifest.xml")
                print "Finish building %s!" % apk

    def do_build_all(self, line):
        version = line
        for idx, market in enumerate(APKBuilder.markets):
            print "building for %s" % market
            self.do_build(' '.join([str(idx + 1), version]))

    def help_build_all(self):
        print "build_all 1020x {pwd}"
        

if __name__=="__main__":
    builder = APKBuilder()
    builder.cmdloop()
