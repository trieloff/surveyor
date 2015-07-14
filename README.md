suveyor
=======


Surveyor is a small tool that helps teams looking to implement Outcome-based product management and classify and categorize
features based on systematic user feedback. For this purpose, Surveyor will review your roadmap, extract features and their
outcomes and create a structured survey with following elements:

- measurement of customer satisfaction using Net Promoter Score (NPS)
- measurement of feature opportunity using the Ulwick Opportunity Algorithm
- classifcation of features as *must-be*, *one-dimensional*, and *attractive* using the Kano model

The created survey can then be distributed among users to gather feedback. Once a sufficient amount of responses have been
collected, surveyor will retrieve the results of the survey, collect and aggregate them and store them within the roadmap, so
that they are available for the entire product team.


## Prerequsites

Using surveyor relies on four tools:

- [aha.io](http://www.aha.io) – is the roadmapping tool that surveyor retrieves features and releases from (at least a Premium account is required to use Aha)
- [FluidSurveys](https://fluidsurveys.com) – is the survey tool that will be used to create surveys and collect responses (at least the Ultra account is required, the free usage tier does not include access to the API)
- [Leningen](http://leiningen.org) – running Surveyor requires building it from source code, and this process is using Leiningen as the build tool (Leinigen is free software)
- (optional) [Heroku](https://www.heroku.com) – Surveyor has been developed to be deployed on Heroku, but can also be run on other PaaS platforms, or run on a self-hosted server (the free usage tier is just fine)
- (optional) [Foreman](http://theforeman.org) – to simulate a local Heroku environment


## Building and Installing

Check out source code using Git from https://github.com/trieloff/suveyor.

Build a standalone version using:

    $ lein uberjar

For local development, run

    $ lein ring server

Make sure you have a local reverse proxy set up that handles SSL for you. Surveyor only runs on HTTPS, as some of the APIs that
Surveyor is using are enforcing HTTPS.


## Prerequsites

### Aha Custom Fields

Surveyor is using two custom fields to track status and metadata of a feature: *outcome* and *survey*. You need to add these in
your aha.io product settings.

1. Log in to aha.io
2. Click *Settings*
3. Select your Product or Product Line that you want to use with Surveyor
4. Click *Custom Fields*
5. Click *+* next to the section labeled *Features*
6. Enter *name*: Outcome
7. Enter *description*: Specify what the outcome of the availability of the feature will be. Good outcomes describe how a measurable KPI is changing
8. Enter *key*: outcome
9. Select placement: *with attributes*
10. Click *Save*
11. Click *+* next to the section labeled *Features*
12. Enter *name*: Survey
13. Enter *description*: URL of the survey used for tracking feature status
14. Enter *key*: survey
15. Select placement: *below record*
16. Click *Save*

### Aha Custom Scores

### Aha Application

In order to use Aha's API, you need to register an application with Aha.

1. Log in to aha.io
2. Open the [Aha Developer Console](https://secure.aha.io/oauth/applications)
3. Click *register new application*
4. Enter a name
5. Enter the Redirect URL. In development mode, the URL is generally `https://localhost/aha.callback`, for production use replace *localhost* with your hostname
6. Click *save*
7. Note *Client ID/Application ID* and *Client Secret* – these are needed for later steps

### FluidSurveys Application

In order to use FluidSurveys' API, you need to register an application with FluidSurveys.

1. Log in to FluidSurveys
2. Open the [FluidSurveys Developer Console](https://fluidsurveys.com/accounts/developer/) – you can also browse there from the username dropdown, by selecting *Developer*
3. Click *create new application*
4. Enter a name
5. Enter the redirect_url. In development mode, the URL is generally `https://localhost:443/fluidsurveys.callback`, for production use replace *localhost* with your hostname. Do not omit the port, otherwise the integration will break
6. Click *Create*
7. Locate your application in the list and click *Manage*
8. Note your *client_id* and *client_secret* – these are needed for later steps


## Configuration

Copy the file surveyor-default.properties into surveyor.properties and edit the settings. Each
setting can also be overridden through an environment variable, so that `aha.host` becomes `$AHA_HOST`. This is useful for a
deployment on Heroku, where you don't want to store security credentials in plain text.

| Configuration Parameter | Environment Variable | Description |
|--------------------|------------------|----------|
| `aha.clientid` | `$AHA_CLIENTID` | The *Client ID/Application ID* provided by Aha.io
| `aha.clientsecret` | `$AHA_CLIENTSECRET` | The *Client Secret* provided by Aha.io
| `aha.clientdomain` | `$AHA_CLIENTDOMAIN` | The hostname where your instance of Surveyor is running. In development you can keep `localhost`, for production replace it with the correct hostname. This should be the same hostname you have been specifying as part of the *Redirect URL* for Aha and *redirect_url* for FluidSurveys.
| `aha.host` | `$AHA_HOST` | The tenant name for the your Aha account. When you are logged in to Aha.io, if the looks like `https://mycompany.aha.io`, you should use `mycompany` as value for this parameter.
| `fluidsurveys.clientid` | `$FLUIDSURVEYS_CLIENTID` | The *client_id* as provided by FluidSurveys
| `fluidsurveys.clientsecret` | `FLUIDSURVEYS_CLIENTSECRET` | The *client_secret* as provided by FluidSurveys

All of these settings are mandatory and required to make Surveyor work.

## Running

There are multiple ways of running Surveyor

### Locally

    $ java -cp target/uberjar/surveyor-standalone.jar clojure.main -m surveyor.repl 3000

### Using Leiningen

    $ lein ring server

### Using Foreman

    $ foreman start


### On Heroku

Follow the [Heroku Clojure Guide](https://devcenter.heroku.com/articles/getting-started-with-clojure#introduction) to install
Surveyor on Heroku. Do not forget to set the configuration options as environment variables.

## Usage

### Plan your Roadmap

Use Aha.io to create features and group them into releases. For each feature that should be considered for the survey, define the *Outcome* in the feature metadata.

### 1. Create the Survey

1. Open Surveyor
2. Click *Log in with FluidSurveys and Aha.io*
3. Surveyor will redirect you to a FluidSurveys authentiaction screen. *Accept* the request
4. Surveyor will then redirect you to an equivalent Aha.io authentiaction screen. *Accept* this request as well
5. Select the product you want to generate a survey for. All products will show up, but only products with the appropriate settings (custom fields and scores) will work with Surveyor
6. Select the releases you want to include in your survey. You can either check the checkbox for multiple releases or click on a specific release
7. (Optional) Select the additional options, if needed
8. Click *create release survey* or *create multi-release survey*
9. Copy the Survey URL

### 2. Distribute the Survey

Distribute the survey among your users. You can use in-application messaging or e-mail for this.

### 3. Wait for Responses

Patiently

### 4. Gather responses

1. Open Surveyor
2. Click *Log in with FluidSurveys and Aha.io*
3. Authenticate, if needed
4. Select the product you want to gather feedback for
5. Select the release you want to gather feedback for
6. Click *retrieve results*

Your survey results will now be merged back into Aha.io

### Review the Roadmap

In your roadmap, you will now see following things:

- Some features have the tag *must-be*
- Some features have the tag *one-dimensional*
- Some features have the tag *attractive*
- All features have a new Aha Score
- Clicking on the Aha score, will show scores for *Opportunity*, *Importance*, and *Satisfaction*

Use this information to re-plan your roadmap.

## Options

- *Override scores* – by default, Surveyor will not include features in the survey that already have a score. Check this option to include features that have (manually set) scores
- *Override survey links* – by default, Surveyor will not include features in the survey that have been covered in other surveys before. Check this option to include all features. **Warning** this will break the link to the older survey, so that updates to the older survey's results will not be reflected on the affected features.
- *Inlcude deleted features* –  by default, Surveyor will not include features that are in the *will not implement* state. Check this option to override the behavior.

### Bugs

plenty.

### Build Status

[![Circle CI](https://circleci.com/gh/trieloff/suveyor.svg?style=svg&circle-token=fcccfd19fa0612bc8a781777ea28df7ed313615f)](https://circleci.com/gh/trieloff/suveyor)


## License

Copyright © 2014 Lars Trieloff

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
