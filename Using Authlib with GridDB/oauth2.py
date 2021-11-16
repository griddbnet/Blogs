from authlib.integrations.flask_oauth2 import AuthorizationServer, ResourceProtector
from authlib.oauth2.rfc6749 import grants
from werkzeug.security import gen_salt
import bcrypt
from authlib.oauth2.rfc6750 import BearerTokenValidator
from authlib.oauth2.rfc7009 import RevocationEndpoint
from flask import abort
import datetime
import time
import os
from .griddb import griddb, gridstore 
from .models import *

class PasswordGrant(grants.ResourceOwnerPasswordCredentialsGrant):
    def authenticate_user(self, username, password):
        print("authenticating user") 
        try:

            con = gridstore.get_container("USERS")
            q = con.query("select * where username = '"+username+"'")
            rs = q.fetch(False)
            if rs.has_next():
                u = rs.next()
        except:
            print("Exception finding user")
            abort(401, "UNAUTHORIZED")
        if u:
            user = User(u)
            if user.check_password(password):
                return user
            print("pass doesnt match")

        abort(401, "UNAUTHORIZED")
            
class RefreshTokenGrant(grants.RefreshTokenGrant):
    def authenticate_refresh_token(self, refresh_token):
        return None

    def authenticate_user(self, credential):
        return None


class MyBearerTokenValidator(BearerTokenValidator):
    def authenticate_token(self, token_string):
        try:
            cn = gridstore.get_container("TOKENS")
            q = cn.query("select * where token = '"+token_string+"'")
            rs = q.fetch(False)
            if rs.has_next():
                data = rs.next()
                data[1] = data[1].timestamp() #oauth expects floats, not datetime
                data[2] = data[2].timestamp()
                print("Token Model: " + repr(data))
                return OAuth2Token(data)
            else:
                print("found nothing in query?")
                return None
        except:
            print("issue with getting container")
            return None
        return None

    def request_invalid(self, request):
        return False

    def token_revoked(self, token):
        return False

def query_client(client):
    return OAuth2Client()

def save_token(token, request):
    #database.write_token(token['access_token'], token['expires_in'], request.user.username)

    print("saving token")
    conInfo = griddb.ContainerInfo("TOKENS",
        [["token", griddb.Type.STRING],
        ["expires_at", griddb.Type.TIMESTAMP],
        ["issued_at", griddb.Type.TIMESTAMP],
        ["userid", griddb.Type.STRING]],
        griddb.ContainerType.COLLECTION, True)
    cn = gridstore.put_container(conInfo)

    cn.set_auto_commit(False)
    cn.create_index("token")
    cn.put([token['access_token'], datetime.datetime.utcfromtimestamp(time.time()+token['expires_in']), datetime.datetime.utcnow(), request.user.username])
    cn.commit()

authorization = AuthorizationServer()
require_oauth = ResourceProtector()
require_oauth.register_token_validator(MyBearerTokenValidator())

def config_oauth(app):
    
    authorization.init_app(
        app, query_client=query_client, save_token=save_token)

    # support all grants
    authorization.register_grant(grants.ImplicitGrant)
    authorization.register_grant(grants.ClientCredentialsGrant)
    authorization.register_grant(PasswordGrant)

    # protect resource
    require_oauth.register_token_validator(MyBearerTokenValidator())
