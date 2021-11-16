import json
import base64
import time
import bcrypt
from flask import Blueprint, request, session, url_for
from flask import render_template, redirect, jsonify, make_response
from werkzeug.security import gen_salt
from authlib.integrations.flask_oauth2 import current_token
from authlib.oauth2 import OAuth2Error
from .models import User, OAuth2Client
from .oauth2 import authorization, require_oauth
from .griddb import griddb, gridstore

bp = Blueprint('home', __name__)

def require_cookie_oauth(request, func):
    if not request.headers.get('Authorization'):
        token = request.cookies.get('access_token')
        my_headers = {}
        for key in request.headers.keys():
            my_headers[key] = request.headers[key]
        my_headers['Authorization'] = "Bearer %s" %token 
    request.headers=my_headers

    return require_oath(*args, **kwargs) 

def get_token(request):

    token = None
    if request.headers.get('Authorization'):
        token = request.headers.get('Authorization').split(" ")[1]
    elif request.cookies.get('access_token') != None:
        token = request.cookies.get('access_token')

    return token

def split_by_crlf(s):
    return [v for v in s.splitlines() if v]


@bp.route('/', methods=('GET', 'POST'))
def home():
    if request.method == 'POST':
        print(request.headers)
        username = request.form.get('username')
        password = request.form.get('password')

        print("Form data: " + username + " " + password)

        my_headers = {}
        for key in request.headers.keys():
            my_headers[key] = request.headers[key]

        base64string = base64.b64encode("1234:abcdef".encode()).decode()
        my_headers['Authorization'] = 'Basic %s ' %base64string 
        request.headers=my_headers

        resp = authorization.create_token_response(request=request)
        if resp.status_code == 200:
            print("resp",dir(resp))
            access_token = json.loads(resp.data)['access_token']
            resp.set_cookie('access_token', access_token)
            return access_token

        return resp

    return render_template('home.html' )


@bp.route('/logout')
def logout():
    resp = make_response(render_template("home.html"))
    resp.set_cookie('access_token', '')
    return resp 


@bp.route('/sign_up', methods=('GET', 'POST'))
def sign_up():
#    user = current_user()

#   if user:
#       return redirect('/')

    if request.method == 'GET':
        return render_template('sign_up.html')

    print("adding user")

    conInfo = griddb.ContainerInfo("USERS",
        [["username", griddb.Type.STRING],
         ["password", griddb.Type.BLOB]],
        griddb.ContainerType.COLLECTION, True)

    cn = gridstore.put_container(conInfo)
    form = request.form
    passBytes = bytearray(bcrypt.hashpw(form['password'].encode('utf8'), bcrypt.gensalt()))
    cn.put([form['username'], passBytes])

    return redirect('/')

@bp.route('/private')
@require_oauth("all")
def private():
    return "This is a private page only viewable if you're logged in"

