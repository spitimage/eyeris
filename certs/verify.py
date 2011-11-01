from M2Crypto.X509 import load_cert, load_cert_string
import base64

# Note: This doesn't seem to work with DSA
# You can also load certs from a string with load_cert_string


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

def dobase64Str():
	f = open('signature.txt')

	b64 = f.read()

	signature = base64.b64decode(b64)	

	cf = open('new_cert.x509')

	certString = cf.read()

	c = load_cert_string(certString)

	k = c.get_pubkey()

	k.verify_init()

	data = 'Jesus is Lord'

	k.verify_update(data)

	result = k.verify_final(signature)

	print 'verification result: ', result

dobase64Str()


