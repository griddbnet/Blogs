import time
import bcrypt
from authlib.oauth2.rfc6749 import ClientMixin, TokenMixin, AuthorizationCodeMixin
from .griddb import griddb, gridstore
import enum
class statusEnum(enum.Enum):
    active = 1
    inactive = 2

class User():

    def __init__(self, user):
        if user == None:
            return None
        self.user = user
        self.username = self.user[0]

    def __str__(self):
        return self.username

    def get_user_id(self):
        return self.username

    def check_password(self, password):
        if bcrypt.checkpw(password.encode('utf8'), self.user[1].decode('utf8').encode('utf8')): 
            return True
        return False


class Token():
    def __init__(self, token):
        self.token = token;

    def get(self):
        return token


class OAuth2Client(ClientMixin):
    def check_token_endpoint_auth_method(self, method):
        # XXX - TODO
        return True

    def check_client_secret(self, client_secret):
        # XXX - TODO
        return True

    def check_grant_type(self, grant_type):
        # XXX _ TODO
        return True

    def check_requested_scopes(self, scopes):
        # XXX - TODO
        return True

    def get_allowed_scope(self, scope):
        return scope

class OAuth2AuthorizationCode(AuthorizationCodeMixin):
    foo=""


class OAuth2Token(TokenMixin):
    def __init__(self, token):
        self.token = token
        
    def is_refresh_token_expired(self):
        return False

    def get_expires_at(self):
        return self.token[1]

    def get_scope(self):
        return "all"

    def authenticate_token(self, token_string):
        token = None
        try:
            cn = griddb.get_container("TOKENS")
            q = con.query("select * where token = '"+token_string+"'")
            rs = q.fetch(False)
            if rs.has_next():
                return OAuth2Token(rs.next())
        except: 
            return None
        return None

