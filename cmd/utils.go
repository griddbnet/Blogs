package cmd

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"os"
	"strconv"
	"time"

	"github.com/cqroot/prompt"
	"github.com/spf13/viper"
)

type Columns struct {
	Name          string `json:"name"`
	Type          string `json:"type"`
	TimePrecision string `json:"timePrecision,omitempty"`
}

type ContainerInfoColumns struct {
	Name          string   `json:"name"`
	Type          string   `json:"type"`
	TimePrecision string   `json:"timePrecision,omitempty"`
	Index         []string `json:"index"`
}

type CloudResults struct {
	Offset  int       `json:"offset"`
	Limit   int       `json:"limit"`
	Total   int       `json:"total"`
	Rows    [][]any   `json:"rows"`
	Columns []Columns `json:"columns"`
}

type TQLResults struct {
	Offset           int       `json:"offset"`
	Limit            int       `json:"limit"`
	Total            int       `json:"total"`
	ResponseSizeByte int32     `json:"responseSizeByte,omitempty"`
	Results          [][]any   `json:"results"`
	Columns          []Columns `json:"columns"`
}

type SqlResults struct {
	ResponseSizeByte float32   `json:"responseSizeByte"`
	Results          [][]any   `json:"results"`
	Columns          []Columns `json:"columns"`
}

type QueryData struct {
	Name  string
	Type  string
	Value any
}

type ContainersList struct {
	Names  []string `json:"names"`
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	Total  int      `json:"total"`
}

type ContainerInfo struct {
	ContainerName string                 `json:"container_name"`
	ContainerType string                 `json:"container_type"`
	RowKey        bool                   `json:"rowkey"`
	Columns       []ContainerInfoColumns `json:"columns"`
}

type ErrorMsg struct {
	Version      string `json:"version"`
	ErrorCode    int    `json:"errorCode"`
	ErrorMessage string `json:"errorMessage"`
}

var GridDBTypes = []string{
	"BOOL",
	"STRING",
	"BYTE",
	"SHORT",
	"INTEGER",
	"LONG",
	"FLOAT",
	"DOUBLE",
	"TIMESTAMP",
	"GEOMETRY",
	"BLOB",
	"BOOL_ARRAY",
	"STRING_ARRAY",
	"BYTE_ARRAY",
	"SHORT_ARRAY",
	"INTEGER_ARRAY",
	"LONG_ARRAY",
	"FLOAT_ARRAY",
	"DOUBLE_ARRAY",
	"TIMESTAMP_ARRAY",
}

func AddBasicAuth(req *http.Request) {
	user := viper.GetString("cloud_username")
	pass := viper.GetString("cloud_pass")
	req.SetBasicAuth(user, pass)
}

func MakeNewRequest(method, endpoint string, body io.Reader) (req *http.Request, e error) {

	if !(viper.IsSet("cloud_url")) {
		log.Fatal("Please provide a `cloud_url` in your config file! You can copy this directly from your Cloud dashboard")
	}

	url := viper.GetString("cloud_url")
	req, err := http.NewRequest(method, url+endpoint, body)
	if err != nil {
		fmt.Println("error with request:", err)
		return req, err
	}

	AddBasicAuth(req)
	req.Header.Add("Content-Type", "application/json")
	return req, nil
}

// This check error is for the User prompt stuff
func CheckErr(err error) {
	if err != nil {
		if errors.Is(err, prompt.ErrUserQuit) {
			fmt.Fprintln(os.Stderr, "Error:", err)
			os.Exit(1)
		} else {
			panic(err)
		}
	}
}

type IP struct {
	Query string
}

func getIP() string {
	req, err := http.Get("http://ip-api.com/json/")
	if err != nil {
		return err.Error()
	}
	defer req.Body.Close()

	body, err := io.ReadAll(req.Body)
	if err != nil {
		return err.Error()
	}

	var ip IP
	json.Unmarshal(body, &ip)

	return ip.Query
}

func CheckForErrors(resp *http.Response) {

	//Separated out because a 403 results in a page with no returned body other than raw html
	if resp.StatusCode == 403 {
		log.Fatal("(403) IP Connection Error. Is this IP Address Whitelisted? Please consider whitelisting Ip Address: " + getIP())
	}

	if resp.StatusCode > 299 {
		body, err := io.ReadAll(resp.Body)
		if err != nil {
			fmt.Println("Error with reading body! ", err)
		}

		var errorMsg ErrorMsg

		if err := json.Unmarshal(body, &errorMsg); err != nil {
			panic(err)
		}
		switch resp.StatusCode {
		case 400:
			log.Fatal("400 Error: " + errorMsg.ErrorMessage)
		case 401:
			fmt.Println(errorMsg.ErrorMessage)
			log.Fatal("Authentication Error. Please check your username and password in your config file ")
		case 404:
			log.Fatal("404 (not found) - Does this container exist?")
		case 500:
			log.Fatal("500 error! " + errorMsg.ErrorMessage)
		default:
			log.Fatal("Unknown Error. Please try again.  " + errorMsg.ErrorMessage)
		}
	}

}

func CheckIfUnixTime(unixString string) bool {
	if _, err := strconv.ParseFloat(unixString, 64); err == nil {
		return true
	} else {
		return false
	}

}

func ConvertUnixToTime(unixString string) time.Time {

	// --- Separate the float into integer (seconds) and fractional parts ---

	// math.Modf splits floatTimestamp into its integer and fractional parts.
	// For 1594512094.3859746:
	// secFloat will be 1594512094.0
	// fracSeconds will be 0.3859746...
	floatTimestamp, err := strconv.ParseFloat(unixString, 64)
	if err != nil {
		log.Fatal("Error converting to float64")
	}
	secFloat, fracSeconds := math.Modf(floatTimestamp)
	sec := int64(secFloat)

	// Convert the fractional part of seconds into nanoseconds.
	// 1 second = 1,000,000,000 (or 1e9) nanoseconds.
	// nsec = 0.3859746 * 1e9 = 385974600 (approximately, due to float precision)
	nsec := int64(fracSeconds * 1e9) // nsec = 385974600

	// --- Create the time.Time object ---

	// time.Unix creates a time.Time object from seconds and nanoseconds
	// since the epoch (January 1, 1970 UTC).
	// The resulting time will be in the UTC timezone.
	t := time.Unix(sec, nsec)

	return t
}

func ConvertUnixToTimeInt(unixString int64) time.Time {

	// --- Separate the float into integer (seconds) and fractional parts ---

	// math.Modf splits floatTimestamp into its integer and fractional parts.
	// For 1594512094.3859746:
	// secFloat will be 1594512094.0
	// fracSeconds will be 0.3859746...
	floatTimestamp := float64(unixString)
	secFloat, fracSeconds := math.Modf(floatTimestamp)
	sec := int64(secFloat)

	// Convert the fractional part of seconds into nanoseconds.
	// 1 second = 1,000,000,000 (or 1e9) nanoseconds.
	// nsec = 0.3859746 * 1e9 = 385974600 (approximately, due to float precision)
	nsec := int64(fracSeconds * 1e9) // nsec = 385974600

	// --- Create the time.Time object ---

	// time.Unix creates a time.Time object from seconds and nanoseconds
	// since the epoch (January 1, 1970 UTC).
	// The resulting time will be in the UTC timezone.
	t := time.Unix(sec, nsec)

	return t
}
