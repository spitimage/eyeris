from M2Crypto.X509 import load_cert
import base64

# Note: This doesn't seem to work with DSA

def binary():
	f = open('signature.txt')
	signature = f.read()

	c = load_cert('new_cert.x509')

	k = c.get_pubkey()

	k.verify_init()

	data = 'Jesus is Lord'

	k.verify_update(data)

	result = k.verify_final(signature)

	print 'verification result: ', result

def dobase64():
	f = open('signature.txt')

	b64 = f.read()

	signature = base64.b64decode(b64)	

	c = load_cert('new_cert.x509')

	k = c.get_pubkey()

	k.verify_init()

	data = 'Jesus is Lord'

	k.verify_update(data)

	result = k.verify_final(signature)

	print 'verification result: ', result

dobase64()


